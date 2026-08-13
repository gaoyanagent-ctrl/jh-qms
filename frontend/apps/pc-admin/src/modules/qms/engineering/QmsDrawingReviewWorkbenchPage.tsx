import type { QmsQualityCharacteristic, QmsQualityCharacteristicCreateRequest, QmsQualityCharacteristicReviewRequest, QmsSourceEvidence } from '@iaf/domain-types';
import { AimOutlined, MinusOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { QMS_PERMISSIONS, useHasPermission } from '@iaf/permissions';
import { AppPageContainer, StatusTag } from '@iaf/ui-core';
import { Alert, App, Button, Card, Checkbox, Empty, Form, Input, InputNumber, List, Modal, Segmented, Select, Space, Spin, Tag, Typography, theme } from 'antd';
import { ApiError } from '@iaf/api-client';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { GlobalWorkerOptions, getDocument } from 'pdfjs-dist';
import workerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url';
import { useBulkReviewQmsCharacteristicsMutation, useCreateQmsCharacteristicMutation, useQmsCharacteristicsQuery, useQmsEvidenceQuery, useQmsIntermediateModelQuery, useQmsRevisionFileQuery, useQmsRevisionFilesQuery, useQmsRevisionQuery, useQmsRevisionRoleFileQuery, useReviewQmsCharacteristicMutation } from './hooks';

// Keep the URL versioned independently from the application bundle. This prevents a
// previously cached response with an invalid module MIME type from breaking PDF.js.
GlobalWorkerOptions.workerSrc = `${workerUrl}?v=task-0406-1`;

const confidenceLevel = (value: number) => value >= 0.9 ? 'high' : value >= 0.7 ? 'medium' : 'low';
type PdfTextBox = { text: string; left: number; top: number; width: number; height: number; rotation: number };
const normalizedMarkerText = (value?: string | null) => (value ?? '').normalize('NFKC').replace(/\s+/g, '').toLowerCase();

export const QmsDrawingReviewWorkbenchPage = () => {
  const { t } = useTranslation();
  const { message } = App.useApp();
  const { token } = theme.useToken();
  const navigate = useNavigate();
  const params = useParams<{ revisionId: string }>();
  const revisionId = Number(params.revisionId);
  const validId = Number.isSafeInteger(revisionId) && revisionId > 0;
  const [selected, setSelected] = useState<QmsSourceEvidence>();
  const [editing, setEditing] = useState<QmsQualityCharacteristic>();
  const [creating, setCreating] = useState(false);
  const [selectedCharacteristicIds, setSelectedCharacteristicIds] = useState<number[]>([]);
  const [reviewFilter, setReviewFilter] = useState('all');
  const [typeFilter, setTypeFilter] = useState('all');
  const [form] = Form.useForm<QmsQualityCharacteristicReviewRequest>();
  const canReview = useHasPermission(QMS_PERMISSIONS.qualityCharacteristicReview);
  const [filter, setFilter] = useState<string>('all');
  const [pageNo, setPageNo] = useState(1);
  const [pageCount, setPageCount] = useState(0);
  const [renderError, setRenderError] = useState(false);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const dwgViewerRef = useRef<HTMLDivElement>(null);
  const pdfViewerRef = useRef<HTMLDivElement>(null);
  const pdfContentRef = useRef<HTMLDivElement>(null);
  const dragRef = useRef<{ x: number; y: number; left: number; top: number } | undefined>(undefined);
  const pdfDragRef = useRef<{ x: number; y: number; left: number; top: number } | undefined>(undefined);
  const [viewportSize, setViewportSize] = useState({ width: 0, height: 0 });
  const [pdfTextBoxes, setPdfTextBoxes] = useState<PdfTextBox[]>([]);
  const [dwgZoom, setDwgZoom] = useState(1);
  const [dwgPan, setDwgPan] = useState({ x: 0, y: 0 });
  const [pdfZoom, setPdfZoom] = useState(1);
  const [pdfPan, setPdfPan] = useState({ x: 0, y: 0 });
  const [dwgUrl, setDwgUrl] = useState<string>();
  const revisionQuery = useQmsRevisionQuery(validId ? revisionId : undefined, validId);
  const revision = revisionQuery.data;
  const revisionFilesQuery = useQmsRevisionFilesQuery(validId ? revisionId : undefined, revision?.fileType === 'DWG');
  const companionPdf = revisionFilesQuery.data?.find((item) => item.role === 'PDF_REFERENCE');
  const [dwgViewMode, setDwgViewMode] = useState<'PDF' | 'DWG'>('PDF');
  const fileQuery = useQmsRevisionFileQuery(validId ? revisionId : undefined, validId && revision?.fileType === 'PDF');
  const companionFileQuery = useQmsRevisionRoleFileQuery(validId ? revisionId : undefined, 'PDF_REFERENCE', revision?.fileType === 'DWG' && Boolean(companionPdf));
  const parseResultAvailable = revision?.parseStatus === 'SUCCESS' || revision?.parseStatus === 'PARTIAL_SUCCESS';
  const dimQuery = useQmsIntermediateModelQuery(validId ? revisionId : undefined, validId && parseResultAvailable);
  const evidenceQuery = useQmsEvidenceQuery(validId ? revisionId : undefined, validId && parseResultAvailable);
  const characteristicsQuery = useQmsCharacteristicsQuery(validId ? revisionId : undefined, validId && parseResultAvailable);
  const confirmMutation = useReviewQmsCharacteristicMutation(revisionId, 'confirm', () => { setEditing(undefined); message.success(t('qmsReview.reviewSucceeded')); });
  const rejectMutation = useReviewQmsCharacteristicMutation(revisionId, 'reject', () => message.success(t('qmsReview.rejectSucceeded')));
  const createMutation = useCreateQmsCharacteristicMutation(revisionId, () => { setCreating(false); form.resetFields(); message.success(t('qmsReview.createSucceeded')); });
  const bulkMutation = useBulkReviewQmsCharacteristicsMutation(revisionId, () => { setSelectedCharacteristicIds([]); message.success(t('qmsReview.bulkSucceeded')); });

  const locateCharacteristic = (item: QmsQualityCharacteristic) => {
    const source = evidenceQuery.data?.find((evidenceItem) => evidenceItem.id === item.evidenceId);
    if (source) {
      setSelected(source);
    }
  };
  const openReview = (item: QmsQualityCharacteristic) => {
    setEditing(item);
    form.setFieldsValue({ version: item.version, name: item.name, nominalValue: item.nominalValue,
      upperTolerance: item.upperTolerance, lowerTolerance: item.lowerTolerance, unit: item.unit,
      characteristicType: item.characteristicType, specialCharacteristicCode: item.specialCharacteristicCode,
      inspectionDimension: item.inspectionDimension, referenceDimension: item.referenceDimension,
      idealDimension: item.idealDimension, fitDimension: item.fitDimension, locationDimension: item.locationDimension,
      regulatoryFlag: item.regulatoryFlag, mandatoryInspection: item.mandatoryInspection, comment: item.reviewComment });
    locateCharacteristic(item);
  };
  const openCreate = () => {
    setCreating(true); setEditing(undefined); form.resetFields();
    form.setFieldsValue({ characteristicType: 'DIMENSION', unit: 'mm', inspectionDimension: true,
      referenceDimension: false, idealDimension: false, fitDimension: false, locationDimension: false,
      regulatoryFlag: false, mandatoryInspection: false });
  };

  useEffect(() => {
    if (selected?.pageNo) setPageNo(selected.pageNo);
  }, [selected]);

  useEffect(() => {
    const pdfFile = revision?.fileType === 'PDF' ? fileQuery.data
      : revision?.fileType === 'DWG' && dwgViewMode === 'PDF' ? companionFileQuery.data : undefined;
    if (!pdfFile || !canvasRef.current) return;
    let cancelled = false;
    let renderTask: { promise: Promise<void>; cancel?: () => void } | undefined;
    const render = async () => {
      try {
        setRenderError(false);
        const bytes = new Uint8Array(await pdfFile.arrayBuffer());
        const pdf = await getDocument({ data: bytes }).promise;
        if (cancelled) return;
        setPageCount(pdf.numPages);
        const page = await pdf.getPage(Math.min(pageNo, pdf.numPages));
        const base = page.getViewport({ scale: 1 });
        const scale = Math.min(1.6, 900 / base.width) * pdfZoom;
        const viewport = page.getViewport({ scale });
        const canvas = canvasRef.current!;
        const outputScale = Math.min(window.devicePixelRatio || 1, 1.5);
        canvas.width = Math.floor(viewport.width * outputScale);
        canvas.height = Math.floor(viewport.height * outputScale);
        canvas.style.width = `${viewport.width}px`;
        canvas.style.height = `${viewport.height}px`;
        setViewportSize({ width: viewport.width, height: viewport.height });
        renderTask = page.render({ canvas, canvasContext: canvas.getContext('2d')!, viewport,
          transform: outputScale === 1 ? undefined : [outputScale, 0, 0, outputScale, 0, 0] });
        const [textContent] = await Promise.all([
          page.getTextContent(),
          renderTask.promise
        ]);
        if (cancelled) return;
        const singles: PdfTextBox[] = textContent.items.flatMap((item) => {
          if (!('str' in item) || !item.str.trim()) return [];
          const [x, y] = viewport.convertToViewportPoint(item.transform[4], item.transform[5]);
          const height = Math.max(Math.hypot(item.transform[2], item.transform[3]) * viewport.scale, 3);
          return [{ text: item.str, left: x, top: y - height,
            width: Math.max(item.width * viewport.scale, 3), height,
            rotation: -Math.atan2(item.transform[1], item.transform[0]) }];
        });
        const windows = singles.flatMap((_, start) => Array.from({ length: Math.min(5, singles.length - start) }, (__, offset) => {
          const group = singles.slice(start, start + offset + 1);
          const left = Math.min(...group.map((item) => item.left));
          const top = Math.min(...group.map((item) => item.top));
          const right = Math.max(...group.map((item) => item.left + item.width));
          const bottom = Math.max(...group.map((item) => item.top + item.height));
          return { text: group.map((item) => item.text).join(''), left, top, width: right - left,
            height: bottom - top, rotation: group[0].rotation };
        }));
        setPdfTextBoxes(windows);
      } catch { if (!cancelled) setRenderError(true); }
    };
    void render();
    return () => { cancelled = true; renderTask?.cancel?.(); };
  }, [companionFileQuery.data, dwgViewMode, fileQuery.data, pageNo, pdfZoom, revision?.fileType]);

  const evidence = useMemo(() => (evidenceQuery.data ?? []).filter((item) =>
    filter === 'all' || confidenceLevel(item.confidence) === filter), [evidenceQuery.data, filter]);
  const visibleCharacteristics = useMemo(() => (characteristicsQuery.data ?? []).filter((item) =>
    (reviewFilter === 'all' || item.reviewStatus === reviewFilter) &&
    (typeFilter === 'all' || item.characteristicType === typeFilter)), [characteristicsQuery.data, reviewFilter, typeFilter]);
  const selectedPending = (characteristicsQuery.data ?? []).filter((item) => selectedCharacteristicIds.includes(item.id) && item.reviewStatus === 'PENDING');
  const bulkReview = (decision: 'CONFIRMED' | 'REJECTED') => bulkMutation.mutate({ decision,
    targets: selectedPending.map((item) => ({ id: item.id, version: item.version })) });
  const currentSheet = revision?.fileType === 'DWG' ? dimQuery.data?.model.sheets[0] : dimQuery.data?.model.sheets.find((sheet) =>
    sheet.sheetNo === (selected?.sheetNo ?? String(pageNo)));
  const preview = currentSheet?.preview;
  const selectedEntity = currentSheet?.entities.find((entity) => entity.entityId === selected?.entityId);
  const selectedRotation = typeof selectedEntity?.geometry?.textRotation === 'number'
    ? selectedEntity.geometry.textRotation : 0;
  useEffect(() => {
    if (!preview?.content) { setDwgUrl(undefined); return; }
    const url = URL.createObjectURL(new Blob([preview.content], { type: 'image/svg+xml' }));
    setDwgUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [preview?.content]);
  useEffect(() => {
    const viewer = dwgViewerRef.current;
    if (!viewer || revision?.fileType !== 'DWG' || !preview) return;
    const handleWheel = (event: WheelEvent) => {
      event.preventDefault();
      setDwgZoom((value) => Math.min(6, Math.max(0.5, value + (event.deltaY < 0 ? 0.25 : -0.25))));
    };
    viewer.addEventListener('wheel', handleWheel, { passive: false });
    return () => viewer.removeEventListener('wheel', handleWheel);
  }, [dwgUrl, preview, revision?.fileType]);
  useEffect(() => {
    const viewer = pdfViewerRef.current;
    const pdfVisible = revision?.fileType === 'PDF' || (revision?.fileType === 'DWG' && dwgViewMode === 'PDF');
    if (!viewer || !pdfVisible) return;
    const handleWheel = (event: WheelEvent) => {
      event.preventDefault();
      setPdfZoom((value) => Math.min(6, Math.max(0.5, value + (event.deltaY < 0 ? 0.25 : -0.25))));
    };
    viewer.addEventListener('wheel', handleWheel, { passive: false });
    return () => viewer.removeEventListener('wheel', handleWheel);
  }, [dwgViewMode, revision?.fileType, viewportSize]);
  const centerDwgOnEvidence = (source: QmsSourceEvidence) => {
    if (!preview || !dwgViewerRef.current) return;
    const box = dwgViewerRef.current.getBoundingClientRect();
    const centerX = (source.bbox.x + source.bbox.width / 2 - preview.viewBox.x) / preview.viewBox.width;
    const centerY = (source.bbox.y + source.bbox.height / 2 - preview.viewBox.y) / preview.viewBox.height;
    const zoom = 2.5;
    setDwgZoom(zoom);
    setDwgPan({ x: (0.5 - centerX) * box.width * zoom, y: (0.5 - centerY) * box.height * zoom });
  };
  useEffect(() => {
    if (selected) centerDwgOnEvidence(selected);
    // Re-center only when the selected evidence changes.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected?.id, preview]);
  const selectedText = normalizedMarkerText(selected?.normalizedText || selected?.rawText);
  const dimensionText = selectedText.match(/(?:⌀|r)?\d+(?:\.\d+)?(?:±\d+(?:\.\d+)?)?/)?.[0] ?? '';
  const exactPdfBoxes = selectedText.length >= 3
    ? pdfTextBoxes.filter((box) => normalizedMarkerText(box.text).includes(selectedText)) : [];
  const fallbackPdfBoxes = exactPdfBoxes.length === 0 && dimensionText.length >= 2
    ? pdfTextBoxes.filter((box) => normalizedMarkerText(box.text).includes(dimensionText)) : [];
  const matchedPdfBox = [...exactPdfBoxes, ...fallbackPdfBoxes]
    .sort((a, b) => a.width * a.height - b.width * b.height)[0];
  const approximatePdfBox = selected && preview && viewportSize.width > 0 ? {
    left: (selected.bbox.x - preview.viewBox.x) / preview.viewBox.width * viewportSize.width,
    top: (selected.bbox.y - preview.viewBox.y) / preview.viewBox.height * viewportSize.height,
    width: Math.max(selected.bbox.width / preview.viewBox.width * viewportSize.width, 8),
    height: Math.max(selected.bbox.height / preview.viewBox.height * viewportSize.height, 8),
    rotation: -selectedRotation
  } : undefined;
  const rawPdfOverlay = selected && viewportSize.width > 0 ? matchedPdfBox ?? approximatePdfBox : undefined;
  const pdfMarkerPadding = rawPdfOverlay ? Math.max(rawPdfOverlay.height * 0.45, 6) : 0;
  const pdfOverlay = rawPdfOverlay ? {
    ...rawPdfOverlay,
    left: rawPdfOverlay.left - pdfMarkerPadding,
    top: rawPdfOverlay.top - pdfMarkerPadding,
    width: rawPdfOverlay.width + pdfMarkerPadding * 2,
    height: rawPdfOverlay.height + pdfMarkerPadding * 2
  } : undefined;
  const dwgMarkerPadding = selected && preview
    ? Math.max(selected.bbox.height * 0.45, preview.viewBox.height * 0.001) : 0;
  const dwgOverlay = selected && preview ? {
    left: `${(selected.bbox.x - dwgMarkerPadding - preview.viewBox.x) / preview.viewBox.width * 100}%`,
    top: `${(selected.bbox.y - dwgMarkerPadding - preview.viewBox.y) / preview.viewBox.height * 100}%`,
    width: `${Math.max((selected.bbox.width + dwgMarkerPadding * 2) / preview.viewBox.width * 100, 0.25)}%`,
    height: `${Math.max((selected.bbox.height + dwgMarkerPadding * 2) / preview.viewBox.height * 100, 0.5)}%`,
    transform: `rotate(${-selectedRotation}rad)`
  } : undefined;
  const centerPdfOnEvidence = () => {
    if (!pdfOverlay || !pdfViewerRef.current || !pdfContentRef.current) return;
    const zoom = 2.5;
    if (pdfZoom !== zoom) { setPdfZoom(zoom); return; }
    const centerX = pdfOverlay.left + pdfOverlay.width / 2;
    const centerY = pdfOverlay.top + pdfOverlay.height / 2;
    setPdfPan({
      x: pdfViewerRef.current.clientWidth / 2 - pdfContentRef.current.offsetLeft - centerX,
      y: pdfViewerRef.current.clientHeight / 2 - pdfContentRef.current.offsetTop - centerY
    });
  };
  useEffect(() => {
    if (selected && pdfOverlay && (revision?.fileType === 'PDF' || dwgViewMode === 'PDF')) centerPdfOnEvidence();
    // Re-center after selection, PDF rendering, or switching back to the PDF reference.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected?.id, pdfOverlay?.left, pdfOverlay?.top, dwgViewMode, pdfZoom, revision?.fileType]);
  const modelMissing = Boolean(revision && !parseResultAvailable)
    || (dimQuery.error instanceof ApiError && dimQuery.error.code === 'QMS_INTERMEDIATE_MODEL_NOT_FOUND');

  if (!validId) return <AppPageContainer title={t('qmsReview.title')}><Alert type="error" showIcon message={t('qmsReview.invalidRevision')} /></AppPageContainer>;

  return <AppPageContainer title={`${t('qmsReview.title')} · ${revision?.revisionCode ?? revisionId}`}
    extra={<Button onClick={() => navigate(-1)}>{t('common.actions.back')}</Button>}>
    <Card size="small" style={{ marginBottom: token.marginMD }}>
      <Space wrap>
        <Typography.Text strong>{revision?.fileType ?? t('common.notAvailable')}</Typography.Text>
        {revision && <StatusTag status={revision.status} label={t(`qms.status.${revision.status}`)} />}
        {revision && <StatusTag status={revision.parseStatus} label={t(`qms.status.${revision.parseStatus}`)} />}
        <Typography.Text type="secondary">{t('qmsReview.evidenceCount', { count: evidenceQuery.data?.length ?? 0 })}</Typography.Text>
      </Space>
    </Card>
    {modelMissing && <Alert style={{ marginBottom: token.marginMD }} type="info" showIcon message={t('qmsReview.waitingForParse')} description={t('qmsReview.waitingForParseDescription')} />}
    <div className="qms-review-workbench">
      <Space className="qms-review-list-pane" direction="vertical" size="middle" style={{ width: '100%' }}>
      <Card title={t('qmsReview.characteristics')} extra={canReview && <Button size="small" onClick={openCreate}>{t('qmsReview.manualCreate')}</Button>}>
        <Space wrap size="small" style={{ marginBottom: token.marginSM }}>
          <Segmented size="small" value={reviewFilter} onChange={(value) => setReviewFilter(String(value))}
            options={[{ label: t('qmsReview.filters.all'), value: 'all' }, { label: t('qmsReview.filters.pending'), value: 'PENDING' },
              { label: t('qmsReview.filters.confirmed'), value: 'CONFIRMED' }, { label: t('qmsReview.filters.rejected'), value: 'REJECTED' }]} />
          <Select size="small" aria-label={t('qmsReview.fields.type')} value={typeFilter} onChange={setTypeFilter} style={{ minWidth: 120 }}
            options={[{ label: t('qmsReview.filters.allTypes'), value: 'all' }, { label: t('qmsReview.types.dimension'), value: 'DIMENSION' },
              { label: t('qmsReview.types.appearance'), value: 'APPEARANCE' }, { label: t('qmsReview.types.performance'), value: 'PERFORMANCE' },
              { label: t('qmsReview.types.other'), value: 'OTHER' }]} />
          {canReview && <><Button size="small" disabled={selectedPending.length === 0} loading={bulkMutation.isPending} onClick={() => bulkReview('CONFIRMED')}>{t('qmsReview.bulkConfirm', { count: selectedPending.length })}</Button>
            <Button size="small" danger disabled={selectedPending.length === 0} loading={bulkMutation.isPending} onClick={() => bulkReview('REJECTED')}>{t('qmsReview.bulkReject', { count: selectedPending.length })}</Button></>}
        </Space>
        <List loading={characteristicsQuery.isLoading} dataSource={visibleCharacteristics}
          locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('qmsReview.noCharacteristics')} /> }}
          renderItem={(item) => <List.Item onClick={() => locateCharacteristic(item)} style={{ cursor: item.evidenceId ? 'pointer' : 'default', paddingInline: token.paddingSM }}>
            <div data-testid="characteristic-list-content" style={{ width: '100%', minWidth: 0 }}>
              <Space wrap size={[token.marginXS, token.marginXXS]} style={{ marginBottom: token.marginXXS }}>
                {canReview && item.reviewStatus === 'PENDING' && <Checkbox aria-label={t('qmsReview.selectCharacteristic', { code: item.characteristicCode })}
                  checked={selectedCharacteristicIds.includes(item.id)} onClick={(event) => event.stopPropagation()}
                  onChange={(event) => setSelectedCharacteristicIds((ids) => event.target.checked ? [...ids, item.id] : ids.filter((id) => id !== item.id))} />}
                <Typography.Text strong style={{ overflowWrap: 'anywhere' }}>{item.characteristicCode}</Typography.Text>
                <Tag style={{ marginInlineEnd: 0 }}>{item.reviewStatus}</Tag>
                {item.inspectionDimension && <Tag color="processing">{t('qmsReview.flags.inspection')}</Tag>}
                {item.referenceDimension && <Tag>{t('qmsReview.flags.reference')}</Tag>}
                {item.idealDimension && <Tag>{t('qmsReview.flags.ideal')}</Tag>}
              </Space>
              <Typography.Paragraph type="secondary" style={{ marginBottom: 0, overflowWrap: 'break-word' }}>
                {`${item.name} · ${item.nominalValue ?? '-'} ${item.unit ?? ''} (${item.upperTolerance ?? '-'} / ${item.lowerTolerance ?? '-'})`}
              </Typography.Paragraph>
              {item.reviewStatus === 'PENDING' && canReview && <Space wrap size="small" style={{ display: 'flex', justifyContent: 'flex-end', marginTop: token.marginXS }}>
                <Button size="small" type="link" onClick={(event) => { event.stopPropagation(); openReview(item); }}>{t('qmsReview.editConfirm')}</Button>
                <Button size="small" type="link" danger loading={rejectMutation.isPending} onClick={(event) => { event.stopPropagation(); rejectMutation.mutate({ id: item.id, request: { version: item.version } }); }}>{t('qmsReview.reject')}</Button>
              </Space>}
            </div>
          </List.Item>} />
      </Card>
      <Card title={t('qmsReview.evidence')} extra={<Segmented size="small" value={filter} onChange={(value) => setFilter(String(value))}
        options={[{ label: t('qmsReview.filters.all'), value: 'all' }, { label: t('qmsReview.filters.high'), value: 'high' }, { label: t('qmsReview.filters.medium'), value: 'medium' }, { label: t('qmsReview.filters.low'), value: 'low' }]} />}>
        <List loading={evidenceQuery.isLoading} dataSource={evidence} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('qmsReview.noEvidence')} /> }}
          renderItem={(item) => <List.Item onClick={() => setSelected(item)} style={{ cursor: 'pointer', paddingInline: token.paddingSM, background: selected?.id === item.id ? token.colorPrimaryBg : undefined }}>
            <List.Item.Meta title={<Space><Typography.Text>{item.normalizedText || item.rawText || item.evidenceKey}</Typography.Text><Tag>{Math.round(item.confidence * 100)}%</Tag></Space>}
              description={`${t('qmsReview.sheet')} ${item.sheetNo}${item.pageNo ? ` · ${t('qmsReview.page')} ${item.pageNo}` : ''} · ${item.extractorType}`} />
          </List.Item>} />
      </Card>
      </Space>
      <Card className="qms-review-viewer-card" title={t('qmsReview.viewer')} extra={<Space size="small" wrap>
        {revision?.fileType === 'PDF' && pageCount > 0 && <><Button size="small" disabled={pageNo <= 1} onClick={() => setPageNo((value) => value - 1)}>{t('qmsReview.previousPage')}</Button><Typography.Text>{pageNo}/{pageCount}</Typography.Text><Button size="small" disabled={pageNo >= pageCount} onClick={() => setPageNo((value) => value + 1)}>{t('qmsReview.nextPage')}</Button></>}
        {revision?.fileType === 'DWG' && <>
        {companionPdf && <Segmented size="small" value={dwgViewMode} onChange={(value) => setDwgViewMode(value as 'PDF' | 'DWG')}
          options={[{ label: t('qmsReview.pdfReference'), value: 'PDF' }, { label: t('qmsReview.dwgVector'), value: 'DWG' }]} />}
        </>}
        {(revision?.fileType === 'DWG' && (dwgViewMode === 'DWG' || !companionPdf)) ? <>
        <Button aria-label={t('qmsReview.zoomOut')} icon={<MinusOutlined />} disabled={dwgZoom <= 0.5} onClick={() => setDwgZoom((value) => Math.max(0.5, value - 0.25))} />
        <Typography.Text style={{ minWidth: 48, textAlign: 'center' }}>{Math.round(dwgZoom * 100)}%</Typography.Text>
        <Button aria-label={t('qmsReview.zoomIn')} icon={<PlusOutlined />} disabled={dwgZoom >= 6} onClick={() => setDwgZoom((value) => Math.min(6, value + 0.25))} />
        <Button aria-label={t('qmsReview.resetView')} icon={<ReloadOutlined />} onClick={() => { setDwgZoom(1); setDwgPan({ x: 0, y: 0 }); }} />
        <Button aria-label={t('qmsReview.locateEvidence')} icon={<AimOutlined />} disabled={!selected} onClick={() => selected && centerDwgOnEvidence(selected)} />
        </> : <>
        <Button aria-label={t('qmsReview.zoomOut')} icon={<MinusOutlined />} disabled={pdfZoom <= 0.5} onClick={() => setPdfZoom((value) => Math.max(0.5, value - 0.25))} />
        <Typography.Text style={{ minWidth: 48, textAlign: 'center' }}>{Math.round(pdfZoom * 100)}%</Typography.Text>
        <Button aria-label={t('qmsReview.zoomIn')} icon={<PlusOutlined />} disabled={pdfZoom >= 6} onClick={() => setPdfZoom((value) => Math.min(6, value + 0.25))} />
        <Button aria-label={t('qmsReview.resetView')} icon={<ReloadOutlined />} onClick={() => { setPdfZoom(1); setPdfPan({ x: 0, y: 0 }); }} />
        <Button aria-label={t('qmsReview.locateEvidence')} icon={<AimOutlined />} disabled={!pdfOverlay} onClick={centerPdfOnEvidence} />
        </>}
      </Space>}>
        {fileQuery.isLoading || companionFileQuery.isLoading || ((dwgViewMode === 'DWG' || !companionPdf) && dimQuery.isLoading) ? <Spin /> : revision?.fileType === 'DWG' && (dwgViewMode === 'DWG' || !companionPdf) ? !dwgUrl || !preview ? <Alert type="info" showIcon message={t('qmsReview.cadPreviewMissing')} /> :
          <div ref={dwgViewerRef} data-testid="dwg-viewer" role="img" aria-label={t('qmsReview.dwgCanvas')} tabIndex={0}
            onPointerDown={(event) => { event.currentTarget.setPointerCapture(event.pointerId); dragRef.current = { x: event.clientX, y: event.clientY, left: dwgPan.x, top: dwgPan.y }; }}
            onPointerMove={(event) => { if (dragRef.current) setDwgPan({ x: dragRef.current.left + event.clientX - dragRef.current.x, y: dragRef.current.top + event.clientY - dragRef.current.y }); }}
            onPointerUp={() => { dragRef.current = undefined; }}
            style={{ width: `min(100%, ${72 * preview.viewBox.width / preview.viewBox.height}vh)`, aspectRatio: preview.viewBox.width / preview.viewBox.height,
              marginInline: 'auto', overflow: 'hidden', position: 'relative', cursor: 'grab', background: token.colorFillTertiary, touchAction: 'none' }}>
            <div className="qms-dwg-transform" style={{ position: 'absolute', inset: 0, transform: `translate(${dwgPan.x}px, ${dwgPan.y}px) scale(${dwgZoom})`, transformOrigin: 'center' }}>
              <img src={dwgUrl} alt={t('qmsReview.dwgCanvas')} draggable={false} style={{ width: '100%', height: '100%', objectFit: 'contain', display: 'block' }} />
              {dwgOverlay && <div className="qms-evidence-marker" data-testid="evidence-overlay" aria-label={t('qmsReview.locateEvidence')}
                style={{ position: 'absolute', pointerEvents: 'none', color: token.colorError, transformOrigin: 'center', ...dwgOverlay }} />}
            </div>
          </div> : renderError ? <Alert type="error" showIcon message={t('qmsReview.renderFailed')} /> :
          <div ref={pdfViewerRef} data-testid="pdf-viewer" tabIndex={0} role="img" aria-label={t('qmsReview.pdfCanvas')}
            onPointerDown={(event) => { event.currentTarget.setPointerCapture(event.pointerId); pdfDragRef.current = { x: event.clientX, y: event.clientY, left: pdfPan.x, top: pdfPan.y }; }}
            onPointerMove={(event) => { if (pdfDragRef.current) setPdfPan({ x: pdfDragRef.current.left + event.clientX - pdfDragRef.current.x, y: pdfDragRef.current.top + event.clientY - pdfDragRef.current.y }); }}
            onPointerUp={() => { pdfDragRef.current = undefined; }}
            style={{ overflow: 'hidden', height: '72vh', minHeight: 420, position: 'relative', textAlign: 'center', cursor: 'grab', touchAction: 'none', background: token.colorFillTertiary, padding: token.paddingSM }}>
            {revision?.fileType === 'DWG' && companionPdf && <Alert style={{ marginBottom: token.marginSM, textAlign: 'left' }} type="info" showIcon
              message={t('qmsReview.pdfReferenceRevision', { revision: revision.revisionCode })} />}
            <div ref={pdfContentRef} className="qms-pdf-transform" style={{ display: 'inline-block', position: 'relative', lineHeight: 0,
              transform: `translate(${pdfPan.x}px, ${pdfPan.y}px)`, transformOrigin: 'center' }}>
              <canvas ref={canvasRef} aria-label={t('qmsReview.pdfCanvas')} />
              {pdfOverlay && <div className="qms-evidence-marker" data-testid="evidence-overlay" aria-label={t('qmsReview.locateEvidence')}
                style={{ position: 'absolute', pointerEvents: 'none', color: token.colorError,
                  transform: `rotate(${pdfOverlay.rotation}rad)`, transformOrigin: 'center', left: pdfOverlay.left, top: pdfOverlay.top,
                  width: pdfOverlay.width, height: pdfOverlay.height }} />}
            </div>
          </div>}
      </Card>
    </div>
    <Modal open={Boolean(editing) || creating} title={creating ? t('qmsReview.manualCreate') : t('qmsReview.reviewCharacteristic')}
      confirmLoading={confirmMutation.isPending || createMutation.isPending}
      onCancel={() => { setEditing(undefined); setCreating(false); }} onOk={() => form.validateFields().then((request) => {
        if (creating) createMutation.mutate(request as QmsQualityCharacteristicCreateRequest);
        else if (editing) confirmMutation.mutate({ id: editing.id, request });
      })}>
      <Form form={form} layout="vertical">
        <Form.Item name="version" hidden><InputNumber /></Form.Item>
        <Form.Item name="name" label={t('qmsReview.fields.name')} rules={[{ required: true }]}><Input /></Form.Item>
        <Space align="start" wrap>
          <Form.Item name="characteristicType" label={t('qmsReview.fields.type')} rules={[{ required: true }]}><Select style={{ width: 160 }} options={[
            { label: t('qmsReview.types.dimension'), value: 'DIMENSION' }, { label: t('qmsReview.types.appearance'), value: 'APPEARANCE' },
            { label: t('qmsReview.types.performance'), value: 'PERFORMANCE' }, { label: t('qmsReview.types.other'), value: 'OTHER' }]} /></Form.Item>
          <Form.Item name="specialCharacteristicCode" label={t('qmsReview.fields.specialCode')}><Input style={{ width: 140 }} /></Form.Item>
        </Space>
        <Space align="start" wrap>
          <Form.Item name="nominalValue" label={t('qmsReview.fields.nominal')} rules={[{ required: true }]}><InputNumber /></Form.Item>
          <Form.Item name="upperTolerance" label={t('qmsReview.fields.upperTolerance')}><InputNumber /></Form.Item>
          <Form.Item name="lowerTolerance" label={t('qmsReview.fields.lowerTolerance')}><InputNumber /></Form.Item>
          <Form.Item name="unit" label={t('qmsReview.fields.unit')}><Input style={{ width: 90 }} /></Form.Item>
        </Space>
        <Space wrap>
          {(['inspectionDimension','referenceDimension','idealDimension','fitDimension','locationDimension','regulatoryFlag','mandatoryInspection'] as const).map((field) =>
            <Form.Item key={field} name={field} valuePropName="checked" style={{ marginBottom: token.marginSM }}><Checkbox>{t(`qmsReview.flags.${field}`)}</Checkbox></Form.Item>)}
        </Space>
        <Form.Item name="comment" label={t('qmsReview.fields.comment')}><Input.TextArea rows={2} /></Form.Item>
      </Form>
    </Modal>
  </AppPageContainer>;
};

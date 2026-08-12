import type { QmsSourceEvidence } from '@iaf/domain-types';
import { AppPageContainer, StatusTag } from '@iaf/ui-core';
import { Alert, Button, Card, Empty, List, Segmented, Space, Spin, Tag, Typography, theme } from 'antd';
import { ApiError } from '@iaf/api-client';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { GlobalWorkerOptions, getDocument } from 'pdfjs-dist';
import workerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url';
import { useQmsEvidenceQuery, useQmsIntermediateModelQuery, useQmsRevisionFileQuery, useQmsRevisionQuery } from './hooks';

// Keep the URL versioned independently from the application bundle. This prevents a
// previously cached response with an invalid module MIME type from breaking PDF.js.
GlobalWorkerOptions.workerSrc = `${workerUrl}?v=task-0406-1`;

const confidenceLevel = (value: number) => value >= 0.9 ? 'high' : value >= 0.7 ? 'medium' : 'low';

export const QmsDrawingReviewWorkbenchPage = () => {
  const { t } = useTranslation();
  const { token } = theme.useToken();
  const navigate = useNavigate();
  const params = useParams<{ revisionId: string }>();
  const revisionId = Number(params.revisionId);
  const validId = Number.isSafeInteger(revisionId) && revisionId > 0;
  const [selected, setSelected] = useState<QmsSourceEvidence>();
  const [filter, setFilter] = useState<string>('all');
  const [pageNo, setPageNo] = useState(1);
  const [pageCount, setPageCount] = useState(0);
  const [renderError, setRenderError] = useState(false);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [viewportSize, setViewportSize] = useState({ width: 0, height: 0 });
  const fileQuery = useQmsRevisionFileQuery(validId ? revisionId : undefined, validId);
  const revisionQuery = useQmsRevisionQuery(validId ? revisionId : undefined, validId);
  const revision = revisionQuery.data;
  const parseResultAvailable = revision?.parseStatus === 'SUCCESS' || revision?.parseStatus === 'PARTIAL_SUCCESS';
  const dimQuery = useQmsIntermediateModelQuery(validId ? revisionId : undefined, validId && parseResultAvailable);
  const evidenceQuery = useQmsEvidenceQuery(validId ? revisionId : undefined, validId && parseResultAvailable);

  useEffect(() => {
    if (selected?.pageNo) setPageNo(selected.pageNo);
  }, [selected]);

  useEffect(() => {
    if (!fileQuery.data || revision?.fileType !== 'PDF' || !canvasRef.current) return;
    let cancelled = false;
    const render = async () => {
      try {
        setRenderError(false);
        const bytes = new Uint8Array(await fileQuery.data.arrayBuffer());
        const pdf = await getDocument({ data: bytes }).promise;
        if (cancelled) return;
        setPageCount(pdf.numPages);
        const page = await pdf.getPage(Math.min(pageNo, pdf.numPages));
        const base = page.getViewport({ scale: 1 });
        const scale = Math.min(1.6, 900 / base.width);
        const viewport = page.getViewport({ scale });
        const canvas = canvasRef.current!;
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        setViewportSize({ width: viewport.width, height: viewport.height });
        await page.render({ canvas, canvasContext: canvas.getContext('2d')!, viewport }).promise;
      } catch { if (!cancelled) setRenderError(true); }
    };
    void render();
    return () => { cancelled = true; };
  }, [fileQuery.data, pageNo, revision?.fileType]);

  const evidence = useMemo(() => (evidenceQuery.data ?? []).filter((item) =>
    filter === 'all' || confidenceLevel(item.confidence) === filter), [evidenceQuery.data, filter]);
  const currentSheet = dimQuery.data?.model.sheets.find((sheet) =>
    sheet.sheetNo === (selected?.sheetNo ?? String(pageNo)));
  const overlay = selected && currentSheet && viewportSize.width > 0 ? {
    left: selected.bbox.x / currentSheet.width * viewportSize.width,
    top: selected.bbox.y / currentSheet.height * viewportSize.height,
    width: selected.bbox.width / currentSheet.width * viewportSize.width,
    height: selected.bbox.height / currentSheet.height * viewportSize.height
  } : undefined;
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
    <div style={{ display: 'grid', gridTemplateColumns: 'minmax(260px, 340px) minmax(0, 1fr)', gap: token.marginMD, alignItems: 'start' }}>
      <Card title={t('qmsReview.evidence')} extra={<Segmented size="small" value={filter} onChange={(value) => setFilter(String(value))}
        options={[{ label: t('qmsReview.filters.all'), value: 'all' }, { label: t('qmsReview.filters.high'), value: 'high' }, { label: t('qmsReview.filters.medium'), value: 'medium' }, { label: t('qmsReview.filters.low'), value: 'low' }]} />}>
        <List loading={evidenceQuery.isLoading} dataSource={evidence} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('qmsReview.noEvidence')} /> }}
          renderItem={(item) => <List.Item onClick={() => setSelected(item)} style={{ cursor: 'pointer', paddingInline: token.paddingSM, background: selected?.id === item.id ? token.colorPrimaryBg : undefined }}>
            <List.Item.Meta title={<Space><Typography.Text>{item.normalizedText || item.rawText || item.evidenceKey}</Typography.Text><Tag>{Math.round(item.confidence * 100)}%</Tag></Space>}
              description={`${t('qmsReview.sheet')} ${item.sheetNo}${item.pageNo ? ` · ${t('qmsReview.page')} ${item.pageNo}` : ''} · ${item.extractorType}`} />
          </List.Item>} />
      </Card>
      <Card title={t('qmsReview.viewer')} extra={pageCount > 0 && <Space><Button size="small" disabled={pageNo <= 1} onClick={() => setPageNo((value) => value - 1)}>{t('qmsReview.previousPage')}</Button><Typography.Text>{pageNo}/{pageCount}</Typography.Text><Button size="small" disabled={pageNo >= pageCount} onClick={() => setPageNo((value) => value + 1)}>{t('qmsReview.nextPage')}</Button></Space>}>
        {fileQuery.isLoading ? <Spin /> : revision?.fileType === 'DWG' ? <Alert type="info" showIcon message={t('qmsReview.cadPending')} /> : renderError ? <Alert type="error" showIcon message={t('qmsReview.renderFailed')} /> :
          <div style={{ overflow: 'auto', maxHeight: '72vh', textAlign: 'center', background: token.colorFillTertiary, padding: token.paddingSM }}>
            <div style={{ display: 'inline-block', position: 'relative', lineHeight: 0 }}>
              <canvas ref={canvasRef} aria-label={t('qmsReview.pdfCanvas')} />
              {overlay && <div data-testid="evidence-overlay" style={{ position: 'absolute', pointerEvents: 'none', border: `3px solid ${token.colorError}`, background: token.colorErrorBg, opacity: 0.72, ...overlay }} />}
            </div>
          </div>}
      </Card>
    </div>
  </AppPageContainer>;
};

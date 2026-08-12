from __future__ import annotations

import hashlib
from typing import Any

import pypdfium2 as pdfium

SCHEMA_VERSION = "1.0.0"
PARSER_VERSION = "pdf-vector-0.1.0"


class PdfParseError(ValueError):
    pass


def parse_pdf(content: bytes, document_id: str, revision: str) -> dict[str, Any]:
    if not content.startswith(b"%PDF-"):
        raise PdfParseError("The uploaded content is not a PDF")
    try:
        document = pdfium.PdfDocument(content)
    except Exception as exc:
        raise PdfParseError("The PDF structure cannot be opened") from exc
    if len(document) < 1:
        raise PdfParseError("The PDF has no pages")

    entities: list[dict[str, Any]] = []
    evidence: list[dict[str, Any]] = []
    sheets: list[dict[str, Any]] = []
    digest = hashlib.sha256(content).hexdigest()[:12]

    for page_index in range(len(document)):
        page = document[page_index]
        sheet_no = str(page_index + 1)
        sheet_entities: list[dict[str, Any]] = []
        text_page = page.get_textpage()
        text = text_page.get_text_bounded()
        normalized = " ".join(text.split())
        if normalized:
            boxes = [text_page.get_charbox(index) for index in range(text_page.count_chars())
                     if text_page.get_text_range(index, 1).strip()]
            left = min(box[0] for box in boxes)
            bottom = min(box[1] for box in boxes)
            right = max(box[2] for box in boxes)
            top = max(box[3] for box in boxes)
            entity_id = f"pdf-{digest}-p{sheet_no}-b1"
            evidence_key = f"ev-{entity_id}"
            bbox = {"x": left, "y": page.get_height() - top,
                    "width": right - left, "height": top - bottom}
            entity = {
                "entityId": entity_id,
                "sourceEntityHandle": None,
                "entityType": "TEXT",
                "layer": None,
                "sheetNo": sheet_no,
                "bbox": bbox,
                "geometry": None,
                "rawText": text.rstrip(),
                "normalizedText": normalized,
                "style": None,
                "evidence": [{"evidenceKey": evidence_key}],
            }
            sheet_entities.append(entity)
            entities.append(entity)
            evidence.append({
                "evidenceKey": evidence_key,
                "entityId": entity_id,
                "entityHandle": None,
                "sheetNo": sheet_no,
                "pageNo": page_index + 1,
                "bbox": bbox,
                "rawText": text.rstrip(),
                "normalizedText": normalized,
                "extractorType": "PDF_VECTOR",
                "extractorVersion": PARSER_VERSION,
                "modelName": None,
                "modelVersion": None,
                "confidence": 1.0,
            })
        sheets.append({
            "sheetNo": sheet_no,
            "width": page.get_width(),
            "height": page.get_height(),
            "titleBlock": {},
            "views": [],
            "entities": sheet_entities,
            "notes": [],
            "characteristicCandidates": [],
        })

    model = {
        "schemaVersion": SCHEMA_VERSION,
        "documentId": document_id,
        "revision": revision,
        "sheets": sheets,
    }
    return {
        "schemaVersion": SCHEMA_VERSION,
        "documentId": document_id,
        "revisionCode": revision,
        "modelJson": model,
        "entities": entities,
        "evidence": evidence,
        "parserVersion": PARSER_VERSION,
    }

from __future__ import annotations

import hashlib
import html
import json
import math
import re
import subprocess
import tempfile
from pathlib import Path
from typing import Any

from .parser import SCHEMA_VERSION

PARSER_VERSION = "libredwg-0.14"
LIBREDWG_BIN = Path("/opt/libredwg/bin")
SUPPORTED_ENTITIES = {"TEXT", "MTEXT", "TOLERANCE", "LEADER", "MLEADER"}


class CadParseError(ValueError):
    pass


def _handle(value: Any) -> str | None:
    return format(value[-1], "X") if isinstance(value, list) and value and isinstance(value[-1], int) else None


def _clean_text(value: str) -> str:
    value = re.sub(r"\\[A-Za-z][^;]*;", "", value)
    return value.replace("{", "").replace("}", "").replace("\\P", " ").strip()


def _points(item: dict[str, Any]) -> list[tuple[float, float]]:
    points: list[tuple[float, float]] = []
    entity = str(item.get("entity", ""))
    keys = (
        ("text_midpt", "def_pt", "xline1_pt", "xline2_pt")
        if entity.startswith("DIMENSION_") else
        ("ins_pt", "point") if entity in {"TEXT", "MTEXT", "TOLERANCE"} else
        ("points", "origin") if entity in {"LEADER", "MLEADER"} else
        ("start", "end", "center", "point", "ins_pt")
    )
    for key in keys:
        value = item.get(key)
        if isinstance(value, list) and len(value) in (2, 3) and all(isinstance(v, (int, float)) for v in value):
            if math.isfinite(value[0]) and math.isfinite(value[1]):
                points.append((float(value[0]), float(value[1])))
        elif key == "points" and isinstance(value, list):
            for point in value:
                if isinstance(point, list) and len(point) in (2, 3) and all(isinstance(v, (int, float)) for v in point):
                    if math.isfinite(point[0]) and math.isfinite(point[1]):
                        points.append((float(point[0]), float(point[1])))
    return points


def _bbox(item: dict[str, Any]) -> dict[str, float]:
    points = _points(item)
    if not points:
        points = [(0.0, 0.0)]
    xs, ys = [p[0] for p in points], [p[1] for p in points]
    width = max(xs) - min(xs)
    height = max(ys) - min(ys)
    if item.get("entity") in {"TEXT", "MTEXT"}:
        width = max(width, float(item.get("extents_width") or item.get("rect_width") or 1.0))
        height = max(height, float(item.get("extents_height") or item.get("text_height") or 1.0))
    return {"x": min(xs), "y": min(ys), "width": max(width, 0.01), "height": max(height, 0.01)}


def _dimension_text(item: dict[str, Any]) -> str:
    measurement = float(item.get("act_measurement", 0.0))
    rendered = f"{measurement:.6f}".rstrip("0").rstrip(".")
    user_text = _clean_text(str(item.get("user_text") or ""))
    return user_text.replace("<>", rendered) if user_text else rendered


def _inside_viewbox(item: dict[str, Any], viewbox: dict[str, float]) -> bool:
    x0, y0 = viewbox["x"], viewbox["y"]
    x1, y1 = x0 + viewbox["width"], y0 + viewbox["height"]
    return any(x0 <= x <= x1 and y0 <= y <= y1 for x, y in _points(item))


def _svg_preview(source: Path) -> dict[str, Any]:
    try:
        process = subprocess.run(
            [str(LIBREDWG_BIN / "dwg2SVG"), "--mspace", str(source)], capture_output=True,
            check=False, timeout=120, text=True, encoding="utf-8", errors="replace",
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        raise CadParseError("DWG SVG renderer execution failed") from exc
    if process.returncode != 0 or "<svg" not in process.stdout:
        raise CadParseError(f"DWG SVG renderer rejected the file (exit {process.returncode})")
    svg = process.stdout
    svg = re.sub(r"<(?:script|foreignObject)\b.*?</(?:script|foreignObject)\s*>", "", svg,
                 flags=re.IGNORECASE | re.DOTALL)
    svg = re.sub(r"\son[a-z]+\s*=\s*(?:\"[^\"]*\"|'[^']*')", "", svg, flags=re.IGNORECASE)
    svg = re.sub(r"\s(?:href|xlink:href)=\"(?!#)[^\"]*\"", "", svg, flags=re.IGNORECASE)
    match = re.search(r'viewBox="([\-\d.eE+]+)\s+([\-\d.eE+]+)\s+([\d.eE+]+)\s+([\d.eE+]+)"', svg)
    if not match:
        raise CadParseError("DWG SVG renderer returned an invalid viewBox")
    x, y, width, height = (float(value) for value in match.groups())
    return {"format": "SVG", "content": svg, "viewBox": {"x": x, "y": y, "width": width, "height": height},
            "coordinateSystem": "SVG_NATIVE", "generatedBy": PARSER_VERSION}


def _add_dimension_overlays(preview: dict[str, Any], dimensions: list[dict[str, Any]]) -> None:
    viewbox = preview["viewBox"]
    overlays = ['<g id="jh-qms-native-dimensions" fill="#1677ff" stroke="#1677ff" stroke-width="0.8">']
    font_size = max(min(viewbox["width"], viewbox["height"]) / 180, 2.5)
    for item in dimensions:
        points = _points(item)
        if len(points) >= 2:
            first, second = points[0], points[1]
            overlays.append(f'<line x1="{first[0]}" y1="{first[1]}" x2="{second[0]}" y2="{second[1]}" opacity="0.7"/>')
        midpoint = item.get("text_midpt") or item.get("def_pt")
        if isinstance(midpoint, list) and len(midpoint) >= 2:
            label = html.escape(_dimension_text(item))
            overlays.append(f'<text x="{midpoint[0]}" y="{midpoint[1]}" font-size="{font_size}" stroke="none">{label}</text>')
    overlays.append("</g>")
    preview["content"] = preview["content"].replace("</svg>", "".join(overlays) + "</svg>")


def parse_dwg(content: bytes, document_id: str, revision: str) -> dict[str, Any]:
    if len(content) < 6 or not content.startswith(b"AC10"):
        raise CadParseError("The uploaded content is not a supported DWG")
    with tempfile.TemporaryDirectory(prefix="jh-qms-dwg-") as directory:
        source = Path(directory) / "source.dwg"
        source.write_bytes(content)
        try:
            process = subprocess.run(
                [str(LIBREDWG_BIN / "dwgread"), "-O", "JSON", str(source)], capture_output=True, check=False,
                timeout=120, text=True, encoding="utf-8", errors="replace",
            )
        except (OSError, subprocess.TimeoutExpired) as exc:
            raise CadParseError("DWG parser execution failed") from exc
        if process.returncode != 0:
            raise CadParseError(f"DWG parser rejected the file (exit {process.returncode})")
        try:
            source_model = json.loads(process.stdout)
        except json.JSONDecodeError as exc:
            raise CadParseError("DWG parser returned invalid JSON") from exc
        preview = _svg_preview(source)

    objects = source_model.get("OBJECTS", [])
    layer_names = {_handle(item.get("handle")): item.get("name") for item in objects if item.get("object") == "LAYER"}
    selected = [item for item in objects
                if (str(item.get("entity", "")).startswith("DIMENSION_") or item.get("entity") in SUPPORTED_ENTITIES)
                and _inside_viewbox(item, preview["viewBox"])]
    _add_dimension_overlays(preview, [item for item in selected if str(item.get("entity", "")).startswith("DIMENSION_")])
    digest = hashlib.sha256(content).hexdigest()[:12]
    entities: list[dict[str, Any]] = []
    evidence: list[dict[str, Any]] = []
    all_boxes: list[dict[str, float]] = []
    for index, item in enumerate(selected, 1):
        native_type = str(item["entity"])
        entity_type = "DIMENSION" if native_type.startswith("DIMENSION_") else native_type
        entity_type = entity_type if entity_type in {"TEXT", "MTEXT", "DIMENSION", "LEADER", "MLEADER"} else "OTHER"
        handle = _handle(item.get("handle"))
        entity_id = f"dwg-{digest}-{handle or index}"
        evidence_key = f"ev-{entity_id}"
        raw_text = _dimension_text(item) if entity_type == "DIMENSION" else _clean_text(str(item.get("text") or item.get("text_value") or ""))
        box = _bbox(item)
        all_boxes.append(box)
        layer = layer_names.get(_handle(item.get("layer")))
        geometry = {key: item[key] for key in ("start", "end", "center", "point", "points", "origin", "ins_pt", "text_midpt", "def_pt", "xline1_pt", "xline2_pt", "act_measurement", "dim_rotation") if key in item}
        entity = {"entityId": entity_id, "sourceEntityHandle": handle, "entityType": entity_type,
                  "nativeEntityType": native_type, "layer": layer, "sheetNo": "MODEL", "bbox": box,
                  "geometry": geometry, "rawText": raw_text, "normalizedText": raw_text,
                  "style": {"color": item.get("color"), "lineWeight": item.get("linewt")},
                  "evidence": [{"evidenceKey": evidence_key}]}
        entities.append(entity)
        evidence.append({"evidenceKey": evidence_key, "entityId": entity_id, "entityHandle": handle,
                         "sheetNo": "MODEL", "pageNo": None, "bbox": box, "rawText": raw_text,
                         "normalizedText": raw_text, "extractorType": "DWG_ENTITY",
                         "extractorVersion": PARSER_VERSION, "modelName": None,
                         "modelVersion": None, "confidence": 1.0})
    min_x = min((box["x"] for box in all_boxes), default=0.0)
    min_y = min((box["y"] for box in all_boxes), default=0.0)
    max_x = max((box["x"] + box["width"] for box in all_boxes), default=1.0)
    max_y = max((box["y"] + box["height"] for box in all_boxes), default=1.0)
    model = {"schemaVersion": SCHEMA_VERSION, "documentId": document_id, "revision": revision,
             "sheets": [{"sheetNo": "MODEL", "width": max(max_x - min_x, 1.0),
                         "height": max(max_y - min_y, 1.0), "origin": {"x": min_x, "y": min_y},
                         "titleBlock": {}, "views": [], "entities": entities, "notes": [], "preview": preview,
                         "characteristicCandidates": []}]}
    return {"schemaVersion": SCHEMA_VERSION, "documentId": document_id, "revisionCode": revision,
            "modelJson": model, "entities": entities, "evidence": evidence,
            "parserVersion": PARSER_VERSION}

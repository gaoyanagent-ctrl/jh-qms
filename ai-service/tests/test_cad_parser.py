import json
import subprocess

import pytest

from jh_qms_ai.cad_parser import CadParseError, parse_dwg


def test_parse_dwg_preserves_native_dimension_data(monkeypatch):
    payload = {
        "OBJECTS": [
            {"object": "LAYER", "handle": [5, 16], "name": "细实线"},
            {
                "entity": "DIMENSION_LINEAR",
                "handle": [5, 32],
                "layer": [5, 16],
                "act_measurement": 34.0,
                "user_text": "8-<>",
                "xline1_pt": [0.0, 0.0, 0.0],
                "xline2_pt": [34.0, 0.0, 0.0],
            },
        ]
    }
    monkeypatch.setattr(
        subprocess,
        "run",
        lambda *args, **kwargs: subprocess.CompletedProcess(args[0], 0, json.dumps(payload), ""),
    )

    result = parse_dwg(b"AC1021-test", "drawing-2", "B")

    assert result["parserVersion"] == "libredwg-0.14"
    assert len(result["entities"]) == 1
    entity = result["entities"][0]
    assert entity["sourceEntityHandle"] == "20"
    assert entity["entityType"] == "DIMENSION"
    assert entity["nativeEntityType"] == "DIMENSION_LINEAR"
    assert entity["layer"] == "细实线"
    assert entity["normalizedText"] == "8-34"
    assert entity["geometry"]["act_measurement"] == 34.0
    assert result["evidence"][0]["extractorType"] == "DWG_ENTITY"


def test_parse_dwg_rejects_non_dwg_content():
    with pytest.raises(CadParseError, match="not a supported DWG"):
        parse_dwg(b"%PDF-test", "drawing-2", "B")

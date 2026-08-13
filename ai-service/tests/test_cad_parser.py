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
                "text_midpt": [17.0, -2.0, 0.0],
            },
            {
                "entity": "DIMENSION_LINEAR",
                "handle": [5, 33],
                "ownerhandle": [5, 99],
                "act_measurement": 999.0,
                "text_midpt": [-500.0, 500.0, 0.0],
                "def_pt": [-400.0, 500.0, 0.0],
            },
        ]
    }
    def run(command, *args, **kwargs):
        if str(command[0]).endswith("dwg2SVG"):
            return subprocess.CompletedProcess(command, 0, '<svg viewBox="0 -100 200 100"><script>alert(1)</script></svg>', "")
        return subprocess.CompletedProcess(command, 0, json.dumps(payload), "")
    monkeypatch.setattr(subprocess, "run", run)

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
    preview = result["modelJson"]["sheets"][0]["preview"]
    assert preview["viewBox"] == {"x": 0.0, "y": -100.0, "width": 200.0, "height": 100.0}
    assert preview["coordinateSystem"] == "SVG_NATIVE"
    assert 'id="jh-qms-native-dimensions"' in preview["content"]
    assert ">8-34</text>" in preview["content"]
    assert "<script" not in preview["content"]


def test_parse_dwg_rejects_non_dwg_content():
    with pytest.raises(CadParseError, match="not a supported DWG"):
        parse_dwg(b"%PDF-test", "drawing-2", "B")

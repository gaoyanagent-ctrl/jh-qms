import json
import io
import subprocess

import ezdxf
import pytest

from jh_qms_ai.cad_parser import CadParseError, parse_dwg


def test_parse_dwg_preserves_native_dimension_data(monkeypatch):
    payload = {
        "OBJECTS": [
            {"object": "LAYER", "handle": [5, 16], "name": "细实线"},
            {
                "object": "DIMSTYLE", "handle": [5, 48], "name": "横向",
                "DIMDEC": 2, "DIMTOL": 0, "DIMTP": 0.0, "DIMTM": 0.0, "DIMTXT": 3.0,
                "DIMTIH": 0, "DIMTOH": 0,
            },
            {
                "entity": "DIMENSION_LINEAR",
                "handle": [5, 32],
                "layer": [5, 16],
                "act_measurement": 34.0,
                "user_text": "8-<>",
                "xline1_pt": [0.0, 0.0, 0.0],
                "xline2_pt": [34.0, 0.0, 0.0],
                "text_midpt": [17.0, -2.0, 0.0],
                "dim_rotation": 1.5707963267949,
                "dimstyle": [5, 48],
                "eed": [
                    {"code": 0, "value": "DSTYLE"},
                    {"code": 70, "value": 47}, {"code": 40, "value": 0.3},
                    {"code": 70, "value": 48}, {"code": 40, "value": 0.3},
                    {"code": 70, "value": 71}, {"code": 70, "value": 1},
                    {"code": 70, "value": 271}, {"code": 70, "value": 1},
                ],
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
    drawing = ezdxf.new()
    drawing.modelspace().add_line((0.0, -100.0), (200.0, 0.0))
    dxf_output = io.StringIO()
    drawing.write(dxf_output)
    def run(command, *args, **kwargs):
        if "DXF" in command:
            return subprocess.CompletedProcess(command, 0, dxf_output.getvalue(), "")
        return subprocess.CompletedProcess(command, 0, json.dumps(payload), "")
    monkeypatch.setattr(subprocess, "run", run)

    result = parse_dwg(b"AC1021-test", "drawing-2", "B")

    assert result["parserVersion"] == "libredwg-0.14+ezdxf-1.4.4"
    assert len(result["entities"]) == 1
    entity = result["entities"][0]
    assert entity["sourceEntityHandle"] == "20"
    assert entity["entityType"] == "DIMENSION"
    assert entity["nativeEntityType"] == "DIMENSION_LINEAR"
    assert entity["layer"] == "细实线"
    assert entity["normalizedText"] == "8-34.0±0.3"
    assert entity["geometry"]["act_measurement"] == 34.0
    assert entity["geometry"]["nominalValue"] == 34.0
    assert entity["geometry"]["upperTolerance"] == 0.3
    assert entity["geometry"]["lowerTolerance"] == -0.3
    assert entity["geometry"]["displayPrecision"] == 1
    assert entity["geometry"]["textRotation"] == pytest.approx(1.5707963267949)
    assert result["evidence"][0]["extractorType"] == "DWG_ENTITY"
    preview = result["modelJson"]["sheets"][0]["preview"]
    assert preview["viewBox"]["x"] == 0.0
    assert preview["viewBox"]["y"] == 0.0
    assert preview["sourceViewBox"] == {"x": 0.0, "y": -100.0, "width": 200.0, "height": 100.0}
    assert preview["coordinateSystem"] == "SVG_NATIVE"
    assert 'id="jh-qms-native-dimensions"' not in preview["content"]
    assert "<path" in preview["content"]
    assert "<script" not in preview["content"]
    box = result["evidence"][0]["bbox"]
    assert 0 <= box["x"] <= preview["viewBox"]["width"]
    assert 0 <= box["y"] <= preview["viewBox"]["height"]
    assert box["width"] < preview["viewBox"]["width"] / 2


def test_parse_dwg_rejects_non_dwg_content():
    with pytest.raises(CadParseError, match="not a supported DWG"):
        parse_dwg(b"%PDF-test", "drawing-2", "B")

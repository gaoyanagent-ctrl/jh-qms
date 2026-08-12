from fastapi.testclient import TestClient

from jh_qms_ai.main import app
from jh_qms_ai.parser import parse_pdf


def sample_pdf() -> bytes:
    objects = [
        "<< /Type /Catalog /Pages 2 0 R >>",
        "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 600 400] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
        "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
        "<< /Length 77 >>\nstream\nBT /F1 12 Tf 72 304 Td (PART NO JH-001) Tj 0 -36 Td (DIMENSION 8 +/- 0.5) Tj ET\nendstream",
    ]
    output = bytearray(b"%PDF-1.4\n")
    offsets = [0]
    for number, body in enumerate(objects, 1):
        offsets.append(len(output))
        output.extend(f"{number} 0 obj\n{body}\nendobj\n".encode())
    xref = len(output)
    output.extend(f"xref\n0 {len(objects) + 1}\n0000000000 65535 f \n".encode())
    for offset in offsets[1:]:
        output.extend(f"{offset:010d} 00000 n \n".encode())
    output.extend(f"trailer << /Size 6 /Root 1 0 R >>\nstartxref\n{xref}\n%%EOF\n".encode())
    return bytes(output)


def test_vector_pdf_produces_dim_and_locatable_evidence():
    result = parse_pdf(sample_pdf(), "drawing-1", "D")

    assert result["schemaVersion"] == "1.0.0"
    assert result["modelJson"]["revision"] == "D"
    assert result["modelJson"]["sheets"][0]["width"] == 600
    assert result["entities"][0]["normalizedText"] == "PART NO JH-001 DIMENSION 8 +/- 0.5"
    assert result["evidence"][0]["pageNo"] == 1
    assert result["evidence"][0]["bbox"]["width"] > 0
    assert result["entities"][0]["evidence"][0]["evidenceKey"] == result["evidence"][0]["evidenceKey"]


def test_api_rejects_non_pdf_content():
    response = TestClient(app).post(
        "/internal/v1/pdf/parse",
        data={"document_id": "drawing-1", "revision": "D"},
        files={"file": ("drawing.pdf", b"not pdf", "application/pdf")},
    )
    assert response.status_code == 422
    assert response.json()["detail"] == "The uploaded content is not a PDF"

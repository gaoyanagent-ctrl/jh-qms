from fastapi import FastAPI, File, Form, HTTPException, UploadFile

from .parser import PdfParseError, parse_pdf

app = FastAPI(title="JH QMS AI Service", version="0.1.0")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "OK"}


@app.post("/internal/v1/pdf/parse")
async def parse(
    file: UploadFile = File(...),
    document_id: str = Form(..., min_length=1, max_length=128),
    revision: str = Form(..., min_length=1, max_length=64),
) -> dict:
    content = await file.read()
    try:
        return parse_pdf(content, document_id, revision)
    except PdfParseError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc

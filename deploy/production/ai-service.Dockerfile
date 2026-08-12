FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
WORKDIR /app
COPY ai-service/pyproject.toml ./
COPY ai-service/src ./src
RUN pip install --no-cache-dir .
ENV PYTHONPATH=/app/src
USER 65532:65532
EXPOSE 8000
CMD ["uvicorn", "jh_qms_ai.main:app", "--host", "0.0.0.0", "--port", "8000"]

FROM debian:bookworm-slim AS libredwg-build

ARG LIBREDWG_VERSION=0.14
RUN apt-get update && apt-get install -y --no-install-recommends \
      build-essential ca-certificates curl xz-utils pkg-config libtool autoconf automake \
    && curl -fsSL "https://github.com/LibreDWG/libredwg/releases/download/${LIBREDWG_VERSION}/libredwg-${LIBREDWG_VERSION}.tar.xz" -o /tmp/libredwg.tar.xz \
    && mkdir /tmp/libredwg && tar -xJf /tmp/libredwg.tar.xz -C /tmp/libredwg --strip-components=1 \
    && cd /tmp/libredwg && ./configure --prefix=/opt/libredwg --disable-bindings --disable-python --disable-docs \
    && make -j2 && make install \
    && mkdir -p /opt/libredwg/licenses && cp COPYING /opt/libredwg/licenses/COPYING

FROM python:3.12-slim

RUN apt-get update && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core fonts-noto-cjk \
    && fc-cache -f \
    && rm -rf /var/lib/apt/lists/*

ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1 XDG_CACHE_HOME=/tmp/.cache
WORKDIR /app
COPY --from=libredwg-build /opt/libredwg /opt/libredwg
ENV PATH=/opt/libredwg/bin:$PATH LD_LIBRARY_PATH=/opt/libredwg/lib
COPY ai-service/pyproject.toml ./
COPY ai-service/src ./src
RUN pip install --no-cache-dir .
ENV PYTHONPATH=/app/src
USER 65532:65532
EXPOSE 8000
CMD ["uvicorn", "jh_qms_ai.main:app", "--host", "0.0.0.0", "--port", "8000"]

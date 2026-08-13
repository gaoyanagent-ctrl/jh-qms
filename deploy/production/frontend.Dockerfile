FROM node:22-alpine AS build

RUN corepack enable && corepack prepare pnpm@9.15.9 --activate
WORKDIR /workspace/frontend
COPY frontend/package.json frontend/pnpm-lock.yaml frontend/pnpm-workspace.yaml ./
COPY frontend/apps ./apps
COPY frontend/packages ./packages
COPY frontend/src ./src
COPY frontend/tsconfig.base.json frontend/vitest.config.ts ./

RUN pnpm install --frozen-lockfile \
    && pnpm --filter @iaf/pc-admin build

FROM nginx:1.27-alpine

ARG SOURCE_REVISION=unknown
LABEL org.opencontainers.image.revision=$SOURCE_REVISION

COPY deploy/production/frontend-nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/frontend/apps/pc-admin/dist /usr/share/nginx/html

EXPOSE 80

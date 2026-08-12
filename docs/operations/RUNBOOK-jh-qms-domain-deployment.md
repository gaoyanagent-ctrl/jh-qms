# JH QMS Domain Deployment Runbook

## Topology

`https://iaf-qms.naturedao.tech` terminates TLS at the existing public Nginx instance.
Nginx proxies to the FRP server virtual host; the local FRP client maps that request to
the JH QMS frontend container on `127.0.0.1:15174`. The frontend serves the SPA and
proxies `/api/` to the backend over the private Compose network. The backend is also
bound to `127.0.0.1:18082` for host-local diagnosis. PostgreSQL and MinIO have no host
port mappings.

These ports were selected after checking host listeners. Do not change them without a
fresh `ss -ltnp` and `docker ps` collision check.

## Deploy

```bash
cd /opt/jh-qms
cp deploy/production/.env.example deploy/production/.env
# Replace all password/secret placeholders with separate random values and keep mode 0600.
docker compose --env-file deploy/production/.env \
  -f deploy/production/compose.yml up -d --build
```

The schema seeds tenant `default` and user `admin`. Before public access, replace its
development-only password hash with `{noop}` followed by `JH_QMS_ADMIN_PASSWORD` from
the root-only environment file.

For the first Nginx setup, install the committed HTTP bootstrap vhost, validate and
reload Nginx, then request the certificate with Certbot. After the certificate exists,
replace the bootstrap vhost with the committed HTTPS vhost:

```bash
install -m 0644 deploy/production/iaf-qms.naturedao.tech.http.nginx.conf \
  /etc/nginx/sites-available/iaf-qms
ln -s /etc/nginx/sites-available/iaf-qms /etc/nginx/sites-enabled/iaf-qms
nginx -t
systemctl reload nginx
certbot certonly --webroot -w /var/www/html -d iaf-qms.naturedao.tech
install -m 0644 deploy/production/iaf-qms.naturedao.tech.nginx.conf \
  /etc/nginx/sites-available/iaf-qms
nginx -t
systemctl reload nginx
```

## Verification

```bash
curl -fsS http://127.0.0.1:18082/api/health
curl -fsS -o /dev/null -w '%{http_code}\n' http://127.0.0.1:15174/
curl -fsS https://iaf-qms.naturedao.tech/api/health
docker compose --env-file deploy/production/.env \
  -f deploy/production/compose.yml ps
```

## Safety and rollback

- Only `127.0.0.1:15174` and `127.0.0.1:18082` are published by this stack.
- MinIO remains reachable only from the private Compose network.
- Do not reuse or stop existing host services to resolve a collision.
- Roll back the application with the previous Git commit and rebuild the two images.
- Disable only this vhost by removing `/etc/nginx/sites-enabled/iaf-qms`, validating with
  `nginx -t`, and reloading Nginx. Do not edit unrelated vhosts.
- Preserve the named `jh-qms_postgres-data` and `jh-qms_minio-data` volumes during rollback.

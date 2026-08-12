# TASK-0403 QMS File Service

## Objective

Attach controlled PDF/DWG source files to drawing revisions through a storage-neutral
application contract backed by MinIO. Persist SHA-256 metadata, reject duplicate uploads
for one revision, and expose authenticated metadata/download APIs.

## Acceptance

- Files are limited to PDF/DWG, non-empty, and at most 100 MiB.
- Content is stored under a tenant/revision-scoped opaque object key.
- `qms_file_object` records checksum, size, media type, bucket and object key.
- A revision is atomically bound to one file metadata record and stores file type/checksum.
- Re-uploading identical content to the same revision returns a business conflict.
- Upload and download require QMS revision permissions and upload is audited.
- MinIO is private to the Compose network and has no host port.


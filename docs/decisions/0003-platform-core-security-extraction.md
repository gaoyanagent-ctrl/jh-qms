# ADR-0003: Platform Security Extraction into `iaf-platform-core`

## Status

Accepted

## Context

ADR-0001 fixes the backend module topology and declares:

> `iaf-platform-*` modules may depend on `iaf-platform-core` and `iaf-shared`.

When `iaf-platform-auth` and `iaf-platform-org` were implemented for the
platform user / organization / role / permission management features
(`TASK-0101`), they declared `@RequiresPermission` on every application
service method and relied on a Spring AOP aspect
(`RequiresPermissionAspect`) to enforce the check at method entry.

The annotation, the aspect, and its helper (`PermissionChecker`) were
initially placed in `iaf-platform-permission` — alongside the
`RoleRepository` / `PermissionRepository` data layer. That made every
platform module that wanted declarative permission gating take a
compile-time dependency on the *permission* module, including its data
layer. ADR-0001 has nothing to say about this directly, but it permits
the smaller and clearer "platform modules depend only on
`platform-core` + `shared`" reading, which the current dependency graph
violates.

The permission data layer is genuinely needed only by:

- `iaf-platform-permission` itself (for CRUD and assignment flows).
- The auth filter chain (to resolve permissions for the current user),
  which already lives in a single, well-defined place.

The aspect itself, in contrast, reads only from
`SecurityContext.hasPermission(...)` (an `iaf-shared` API) and never
queries the permission data layer at runtime. So the AOP enforcement
infrastructure has no genuine reason to sit on top of the permission
module.

## Decision

Move the permission-gating surface into a new
`com.company.iaf.platform.core.security` package inside `iaf-platform-core`:

- `RequiresPermission` (annotation)
- `PermissionChecker` (helper)
- `RequiresPermissionAspect` (Spring AOP advice)

Both tests that exercised these classes
(`PermissionCheckerTest`, `RequiresPermissionAspectTest`) move with
them to `iaf-platform-core/src/test`.

After the move:

- `iaf-platform-permission` no longer exports any class that other
  platform modules need in order to apply permission checks.
- `iaf-platform-auth`, `iaf-platform-org`, and any future
  `iaf-platform-*` module need only `iaf-platform-core` (which they
  already depend on) plus `iaf-shared` to write a permission-gated
  application service.
- The permission data layer continues to live in
  `iaf-platform-permission` because that is where the role / permission
  CRUD use cases live.

The earlier cross-module call from `auth` / `org` to
`platform-permission` only existed because of the annotation import;
after the move, none of those application services need to depend on
`iaf-platform-permission`.

`iaf-platform-core`'s POM adds `spring-boot-starter-aop` and
`spring-context` so the aspect can be wired without leaning on
`iaf-platform-permission`.

## Consequences

- ADR-0001 is now honoured for the security-gating layer without an
  explicit amendment; the dependency direction
  `platform-* → platform-core` covers it.
- The annotation package rename forces every service module to update
  its imports; this is captured as part of `TASK-0101` follow-up
  commits so the history stays atomic.
- Aspect-driven permission checks are available to every platform
  module without dragging in the permission data layer.
- The permission data layer stays the single source of truth for which
  permission codes exist in a tenant.
- A future test suite that wants to verify the aspect end-to-end should
  live alongside the aspect itself (`iaf-platform-core`), which keeps
  the test mirror the production code.

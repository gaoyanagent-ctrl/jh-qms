# TASK-0414 Drawing Legend Configuration

## Objective

Provide tenant-managed drawing legend rules and use them to classify parsed quality
characteristics without overwriting human-reviewed records.

## Scope

- Seed configurable rules for ◆, ▲, ★, parenthesized dimensions, [B], [A], and [R].
- Add permission-controlled list/update APIs and an Engineering Data configuration page.
- Apply enabled rules to newly parsed candidates and reclassify pending candidates after save.
- Preserve confirmed/rejected characteristics, tenant/org isolation, audit, and optimistic versioning.

## Acceptance

- Administrators can edit marker, description, enabled state, and priority.
- Rule meanings are fixed to supported classification targets; duplicate markers are rejected.
- `[B]6.5±0.3◆▲` becomes special B, inspection and location dimension.
- Parenthesized dimensions become references and are not inspection dimensions.
- Backend/frontend tests, migration, code maps, and public smoke checks pass.

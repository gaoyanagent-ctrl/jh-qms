-- Advance the PostgreSQL identity sequences for every table whose seed
-- rows were inserted with explicit ids in V0001 and V0002.
--
-- Why: V0001 / V0002 use `insert into ... (id, ...) values (1, ...)`
-- for the bootstrap admin / org / role / permission rows. PostgreSQL
-- does not advance the underlying sequence on explicit inserts, so
-- `nextval(...)` still returns 1. The first `createUser` /
-- `createOrg` / `createRole` that hits the database would therefore
-- collide with the seeded id and fail.
--
-- The setval calls are guarded by `WHERE EXISTS` so the migration is
-- safe on databases that have not yet inserted the seed rows (e.g.
-- partially-applied environments during recovery). The `true` flag
-- means the sequence is marked as `is_called` so the next nextval
-- returns max(id) + 1.

select setval(pg_get_serial_sequence('sys_user', 'id'),
              (select max(id) from sys_user), true)
  where exists (select 1 from sys_user);

select setval(pg_get_serial_sequence('sys_org', 'id'),
              (select max(id) from sys_org), true)
  where exists (select 1 from sys_org);

select setval(pg_get_serial_sequence('sys_role', 'id'),
              (select max(id) from sys_role), true)
  where exists (select 1 from sys_role);

select setval(pg_get_serial_sequence('sys_permission', 'id'),
              (select max(id) from sys_permission), true)
  where exists (select 1 from sys_permission);

select setval(pg_get_serial_sequence('sys_user_role', 'id'),
              (select max(id) from sys_user_role), true)
  where exists (select 1 from sys_user_role);

select setval(pg_get_serial_sequence('sys_role_permission', 'id'),
              (select max(id) from sys_role_permission), true)
  where exists (select 1 from sys_role_permission);

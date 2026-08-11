package com.company.iaf.platform.permission.application;

import com.company.iaf.platform.permission.domain.model.Permission;
import com.company.iaf.platform.permission.domain.model.Menu;
import com.company.iaf.platform.permission.domain.model.Role;
import com.company.iaf.platform.permission.domain.model.RoleStatus;
import com.company.iaf.platform.permission.domain.repository.MenuRepository;
import com.company.iaf.platform.permission.domain.repository.PermissionRepository;
import com.company.iaf.platform.permission.domain.repository.RoleRepository;
import com.company.iaf.platform.permission.interfaces.dto.AssignRoleMenusRequest;
import com.company.iaf.platform.permission.interfaces.dto.AssignRolePermissionsRequest;
import com.company.iaf.platform.permission.interfaces.dto.RoleCreateRequest;
import com.company.iaf.platform.permission.interfaces.dto.RoleResponse;
import com.company.iaf.platform.permission.interfaces.dto.RoleUpdateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleApplicationServiceTest {

    private final InMemoryRoleRepository roleRepository = new InMemoryRoleRepository();
    private final InMemoryPermissionRepository permissionRepository = new InMemoryPermissionRepository();
    private final InMemoryMenuRepository menuRepository = new InMemoryMenuRepository();
    private RoleApplicationService service;

    @BeforeEach
    void setUp() {
        service = new RoleApplicationService(roleRepository, permissionRepository, menuRepository);
        SecurityContext.setUserId(99L);
        permissionRepository.seed(1L, "platform:user:view", "View users");
        permissionRepository.seed(2L, "platform:role:update", "Update roles");
        permissionRepository.seed(3L, "platform:role:assign-permission", "Assign permissions");
        menuRepository.seed(1L, "platform.users", "/platform/users");
        menuRepository.seed(2L, "platform.roles", "/platform/roles");
    }

    @AfterEach
    void clear() {
        SecurityContext.clear();
    }

    @Test
    void createRolePersistsAndRejectsDuplicateCode() {
        RoleCreateRequest request = new RoleCreateRequest(
                "ops", "Operations", "BUSINESS", RoleStatus.ENABLED);

        RoleResponse created = service.createRole(1L, request);

        assertThat(created.id()).isNotNull();
        assertThat(created.roleCode()).isEqualTo("ops");
        assertThat(created.status()).isEqualTo(RoleStatus.ENABLED);

        assertThatThrownBy(() -> service.createRole(1L,
                new RoleCreateRequest("ops", "Dup", "BUSINESS", RoleStatus.ENABLED)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_PERMISSION_ROLE_CODE_ALREADY_EXISTS");
    }

    @Test
    void createRoleIsScopedToTenant() {
        service.createRole(1L, new RoleCreateRequest("r1", "R1", "BUSINESS", RoleStatus.ENABLED));

        RoleResponse otherTenant = service.createRole(2L,
                new RoleCreateRequest("r1", "R1", "BUSINESS", RoleStatus.ENABLED));

        assertThat(otherTenant.tenantId()).isEqualTo(2L);
    }

    @Test
    void updateRoleChangesFieldsAndRejectsDuplicateCode() {
        RoleResponse created = service.createRole(1L,
                new RoleCreateRequest("ops", "Operations", "BUSINESS", RoleStatus.ENABLED));
        service.createRole(1L, new RoleCreateRequest("ops2", "Other", "BUSINESS", RoleStatus.ENABLED));

        RoleResponse updated = service.updateRole(1L, created.id(),
                new RoleUpdateRequest("ops-renamed", "Operations Renamed", "INTERNAL", RoleStatus.ENABLED));

        assertThat(updated.roleCode()).isEqualTo("ops-renamed");
        assertThat(updated.roleType()).isEqualTo("INTERNAL");

        assertThatThrownBy(() -> service.updateRole(1L, created.id(),
                new RoleUpdateRequest("ops2", "Conflict", "INTERNAL", RoleStatus.ENABLED)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_PERMISSION_ROLE_CODE_ALREADY_EXISTS");
    }

    @Test
    void updateRoleThrowsWhenMissing() {
        assertThatThrownBy(() -> service.updateRole(1L, 9_999L,
                new RoleUpdateRequest("ghost", "Ghost", "BUSINESS", RoleStatus.ENABLED)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_PERMISSION_ROLE_NOT_FOUND");
    }

    @Test
    void assignPermissionsReplacesBindingsAtomically() {
        RoleResponse created = service.createRole(1L,
                new RoleCreateRequest("ops", "Operations", "BUSINESS", RoleStatus.ENABLED));
        service.assignPermissions(1L, created.id(),
                new AssignRolePermissionsRequest(List.of("platform:user:view")));

        RoleResponse replaced = service.assignPermissions(1L, created.id(),
                new AssignRolePermissionsRequest(List.of("platform:role:update", "platform:role:assign-permission")));

        assertThat(replaced.permissions()).containsExactlyInAnyOrder("platform:role:update", "platform:role:assign-permission");
    }

    @Test
    void assignPermissionsRejectsUnknownCode() {
        RoleResponse created = service.createRole(1L,
                new RoleCreateRequest("ops", "Operations", "BUSINESS", RoleStatus.ENABLED));

        assertThatThrownBy(() -> service.assignPermissions(1L, created.id(),
                new AssignRolePermissionsRequest(List.of("platform:user:view", "platform:ghost:foo"))))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_PERMISSION_NOT_FOUND");
    }

    @Test
    void assignPermissionsDoesNotLeakPermissionCodesAcrossTenants() {
        RoleResponse created = service.createRole(2L,
                new RoleCreateRequest("ops", "Operations", "BUSINESS", RoleStatus.ENABLED));

        assertThatThrownBy(() -> service.assignPermissions(2L, created.id(),
                new AssignRolePermissionsRequest(List.of("platform:user:view"))))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_PERMISSION_NOT_FOUND");
    }

    @Test
    void assignPermissionsClearsBindingsWhenEmpty() {
        RoleResponse created = service.createRole(1L,
                new RoleCreateRequest("ops", "Operations", "BUSINESS", RoleStatus.ENABLED));
        service.assignPermissions(1L, created.id(),
                new AssignRolePermissionsRequest(List.of("platform:user:view")));

        RoleResponse cleared = service.assignPermissions(1L, created.id(),
                new AssignRolePermissionsRequest(List.of()));

        assertThat(cleared.permissions()).isEmpty();
    }

    @Test
    void assignPermissionsThrowsWhenRoleMissing() {
        assertThatThrownBy(() -> service.assignPermissions(1L, 9_999L,
                new AssignRolePermissionsRequest(List.of("platform:user:view"))))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_PERMISSION_ROLE_NOT_FOUND");
    }

    @Test
    void getRoleReturnsCurrentBindings() {
        RoleResponse created = service.createRole(1L,
                new RoleCreateRequest("ops", "Operations", "BUSINESS", RoleStatus.ENABLED));
        service.assignPermissions(1L, created.id(),
                new AssignRolePermissionsRequest(List.of("platform:user:view", "platform:role:update")));

        RoleResponse response = service.getRole(1L, created.id());

        assertThat(response.permissions()).containsExactlyInAnyOrder("platform:user:view", "platform:role:update");
    }

    @Test
    void listRolesReturnsPageResultEnvelope() {
        service.createRole(1L, new RoleCreateRequest("ops", "Operations", "BUSINESS", RoleStatus.ENABLED));
        service.createRole(1L, new RoleCreateRequest("ops2", "Operations 2", "BUSINESS", RoleStatus.ENABLED));

        var page = service.listRoles(1L, "ops", 1L, 10L);

        assertThat(page.total()).isEqualTo(2L);
        assertThat(page.records()).hasSize(2);
    }

    @Test
    void assignMenusReplacesBindingsAtomically() {
        RoleResponse created = service.createRole(1L,
                new RoleCreateRequest("ops", "Operations", "BUSINESS", RoleStatus.ENABLED));
        service.assignMenus(1L, created.id(), new AssignRoleMenusRequest(List.of("platform.users")));

        RoleResponse replaced = service.assignMenus(1L, created.id(),
                new AssignRoleMenusRequest(List.of("platform.roles")));

        assertThat(replaced.menuCodes()).containsExactly("platform.roles");
    }

    @Test
    void assignMenusRejectsUnknownCode() {
        RoleResponse created = service.createRole(1L,
                new RoleCreateRequest("ops", "Operations", "BUSINESS", RoleStatus.ENABLED));

        assertThatThrownBy(() -> service.assignMenus(1L, created.id(),
                new AssignRoleMenusRequest(List.of("platform.ghost"))))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_PERMISSION_MENU_NOT_FOUND");
    }

    private final class InMemoryRoleRepository implements RoleRepository {

        private final AtomicLong nextId = new AtomicLong(1);
        private final Map<Long, Role> roles = new ConcurrentHashMap<>();
        private final Map<Long, Map<Long, Long>> bindingsByRole = new ConcurrentHashMap<>();

        @Override
        public Optional<Role> findById(long tenantId, long id) {
            Role role = roles.get(id);
            return Optional.ofNullable(role).filter(r -> r.tenantId() == tenantId);
        }

        @Override
        public boolean existsByRoleCode(long tenantId, String roleCode) {
            return roles.values().stream()
                    .anyMatch(role -> role.tenantId() == tenantId && role.roleCode().equals(roleCode));
        }

        @Override
        public List<Role> findPage(long tenantId, String keyword, int pageNo, int pageSize) {
            String needle = keyword == null ? null : keyword.trim().toLowerCase();
            List<Role> filtered = new ArrayList<>();
            for (Role role : roles.values()) {
                if (role.tenantId() != tenantId) continue;
                if (needle == null
                        || role.roleCode().toLowerCase().contains(needle)
                        || role.roleName().toLowerCase().contains(needle)) {
                    filtered.add(role);
                }
            }
            filtered.sort((a, b) -> Long.compare(a.id(), b.id()));
            int from = Math.max(0, (pageNo - 1) * pageSize);
            int to = Math.min(filtered.size(), from + pageSize);
            return new ArrayList<>(filtered.subList(from, to));
        }

        @Override
        public long count(long tenantId, String keyword) {
            String needle = keyword == null ? null : keyword.trim().toLowerCase();
            return roles.values().stream()
                    .filter(role -> role.tenantId() == tenantId)
                    .filter(role -> needle == null
                            || role.roleCode().toLowerCase().contains(needle)
                            || role.roleName().toLowerCase().contains(needle))
                    .count();
        }

        @Override
        public long insert(long operatorUserId, Role role) {
            long id = nextId.getAndIncrement();
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            roles.put(id, new Role(id, role.tenantId(), role.roleCode(), role.roleName(),
                    role.roleType(), role.status(), 0, now, now));
            return id;
        }

        @Override
        public boolean update(long operatorUserId, Role role) {
            Role current = roles.get(role.id());
            if (current == null || current.tenantId() != role.tenantId() || current.version() != role.version()) {
                return false;
            }
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            roles.put(role.id(), new Role(role.id(), role.tenantId(), role.roleCode(), role.roleName(),
                    role.roleType(), role.status(), role.version() + 1,
                    current.createdAt(), now));
            return true;
        }

        @Override
        public boolean updateStatus(long operatorUserId, long tenantId, long id, RoleStatus status, int expectedVersion) {
            Role current = roles.get(id);
            if (current == null || current.tenantId() != tenantId || current.version() != expectedVersion) {
                return false;
            }
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            roles.put(id, new Role(current.id(), current.tenantId(), current.roleCode(), current.roleName(),
                    current.roleType(), status, current.version() + 1, current.createdAt(), now));
            return true;
        }

        @Override
        public boolean replacePermissions(long operatorUserId, long tenantId, long roleId, List<Long> permissionIds) {
            Role role = roles.get(roleId);
            if (role == null || role.tenantId() != tenantId) {
                return false;
            }
            Map<Long, Long> bindings = new HashMap<>();
            if (permissionIds != null) {
                for (Long permissionId : permissionIds) {
                    bindings.put(permissionId, permissionId);
                }
            }
            bindingsByRole.put(roleId, bindings);
            return true;
        }

        Map<Long, Long> bindings(long roleId) {
            return bindingsByRole.getOrDefault(roleId, Map.of());
        }
    }

    private final class InMemoryPermissionRepository implements PermissionRepository {

        private final AtomicLong nextId = new AtomicLong(100);
        private final Map<Long, Permission> permissions = new ConcurrentHashMap<>();
        private final Map<String, Long> codesToId = new ConcurrentHashMap<>();

        void seed(long id, String code, String name) {
            permissions.put(id, new Permission(id, 1L, code, name, "API", "platform", code.substring(code.lastIndexOf(':') + 1)));
            codesToId.put(code, id);
        }

        @Override
        public List<Permission> findAll(long tenantId) {
            return permissions.values().stream()
                    .filter(permission -> permission.tenantId() == tenantId)
                    .sorted((a, b) -> a.permissionCode().compareTo(b.permissionCode()))
                    .toList();
        }

        @Override
        public List<Permission> findAllByCodes(long tenantId, Collection<String> codes) {
            List<Permission> result = new ArrayList<>();
            for (String code : codes) {
                Long id = codesToId.get(code);
                if (id == null) continue;
                Permission permission = permissions.get(id);
                if (permission != null && permission.tenantId() == tenantId) {
                    result.add(permission);
                }
            }
            return result;
        }

        @Override
        public List<Permission> findAllByRoleId(long tenantId, long roleId) {
            Map<Long, Long> bindings = roleRepository.bindings(roleId);
            List<Permission> result = new ArrayList<>();
            for (Long permissionId : bindings.keySet()) {
                Permission permission = permissions.get(permissionId);
                if (permission != null && permission.tenantId() == tenantId) {
                    result.add(permission);
                }
            }
            result.sort((a, b) -> a.permissionCode().compareTo(b.permissionCode()));
            return result;
        }
    }

    private final class InMemoryMenuRepository implements MenuRepository {

        private final Map<Long, Menu> menus = new ConcurrentHashMap<>();
        private final Map<String, Long> codesToId = new ConcurrentHashMap<>();
        private final Map<Long, List<Long>> roleBindings = new ConcurrentHashMap<>();

        void seed(long id, String code, String routePath) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            menus.put(id, new Menu(id, 1L, null, code, "MENU", "menu." + code,
                    routePath, code, "MenuOutlined", (int) id, true, true, 0, now, now));
            codesToId.put(code, id);
        }

        @Override
        public Optional<Menu> findById(long tenantId, long id) {
            Menu menu = menus.get(id);
            return Optional.ofNullable(menu).filter(value -> value.tenantId() == tenantId);
        }

        @Override
        public boolean existsByMenuCode(long tenantId, String menuCode) {
            Long id = codesToId.get(menuCode);
            return id != null && findById(tenantId, id).isPresent();
        }

        @Override
        public List<Menu> findAll(long tenantId) {
            return menus.values().stream().filter(menu -> menu.tenantId() == tenantId).toList();
        }

        @Override
        public List<Menu> findAllByCodes(long tenantId, Collection<String> menuCodes) {
            List<Menu> result = new ArrayList<>();
            for (String code : menuCodes) {
                Long id = codesToId.get(code);
                if (id == null) continue;
                findById(tenantId, id).ifPresent(result::add);
            }
            return result;
        }

        @Override
        public List<Menu> findVisibleByUserId(long tenantId, long userId, Collection<String> permissionCodes) {
            return findAll(tenantId);
        }

        @Override
        public Map<Long, List<String>> permissionCodesByMenuIds(long tenantId, Collection<Long> menuIds) {
            return Map.of();
        }

        @Override
        public List<String> findMenuCodesByRoleId(long tenantId, long roleId) {
            return roleBindings.getOrDefault(roleId, List.of()).stream()
                    .map(menus::get)
                    .filter(menu -> menu != null && menu.tenantId() == tenantId)
                    .map(Menu::menuCode)
                    .toList();
        }

        @Override
        public long insert(long operatorUserId, Menu menu) {
            long id = menus.size() + 1L;
            menus.put(id, new Menu(id, menu.tenantId(), menu.parentId(), menu.menuCode(), menu.menuType(),
                    menu.titleKey(), menu.routePath(), menu.componentKey(), menu.icon(), menu.sortNo(),
                    menu.visible(), menu.enabled(), 0, menu.createdAt(), menu.updatedAt()));
            codesToId.put(menu.menuCode(), id);
            return id;
        }

        @Override
        public boolean update(long operatorUserId, Menu menu) {
            if (!menus.containsKey(menu.id())) return false;
            menus.put(menu.id(), menu);
            return true;
        }

        @Override
        public boolean replaceRoleMenus(long operatorUserId, long tenantId, long roleId, List<Long> menuIds) {
            if (roleRepository.findById(tenantId, roleId).isEmpty()) {
                return false;
            }
            roleBindings.put(roleId, menuIds == null ? List.of() : new ArrayList<>(menuIds));
            return true;
        }
    }
}

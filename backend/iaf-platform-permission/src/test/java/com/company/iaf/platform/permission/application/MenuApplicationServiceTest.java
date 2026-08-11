package com.company.iaf.platform.permission.application;

import com.company.iaf.platform.permission.domain.model.Menu;
import com.company.iaf.platform.permission.domain.repository.MenuRepository;
import com.company.iaf.platform.permission.interfaces.dto.MenuCreateRequest;
import com.company.iaf.platform.permission.interfaces.dto.MenuUpdateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuApplicationServiceTest {

    private final InMemoryMenuRepository repository = new InMemoryMenuRepository();
    private MenuApplicationService service;

    @BeforeEach
    void setUp() {
        service = new MenuApplicationService(repository);
        SecurityContext.setUserId(7L);
        SecurityContext.setPermissions(Set.of("platform:user:view"));
        repository.seed(1L, null, "platform", null, 100);
        repository.seed(2L, 1L, "platform.users", "/platform/users", 110);
        repository.seed(3L, 1L, "platform.roles", "/platform/roles", 120);
        repository.permissionCodesByMenu.put(2L, List.of("platform:user:view"));
        repository.permissionCodesByMenu.put(3L, List.of("platform:role:view"));
    }

    @AfterEach
    void clear() {
        SecurityContext.clear();
    }

    @Test
    void listMenuTreeBuildsHierarchy() {
        var tree = service.listMenuTree(1L);

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).menuCode()).isEqualTo("platform");
        assertThat(tree.get(0).children()).extracting("menuCode").containsExactly("platform.users", "platform.roles");
    }

    @Test
    void currentUserMenusUseUserScopedRepositoryQuery() {
        repository.visibleMenuIdsByUser.put(7L, List.of(2L, 3L));

        var tree = service.listCurrentUserMenus(1L);

        assertThat(tree).extracting("menuCode").containsExactly("platform.users");
    }

    @Test
    void listMenuTreeIsTenantScoped() {
        repository.seed(20L, null, "tenant2.platform", null, 100, 2L);

        var tenant1 = service.listMenuTree(1L);
        var tenant2 = service.listMenuTree(2L);

        assertThat(tenant1).extracting("menuCode").containsExactly("platform");
        assertThat(tenant2).extracting("menuCode").containsExactly("tenant2.platform");
    }

    @Test
    void updateMenuRejectsDescendantParent() {
        repository.seed(4L, 2L, "platform.users.detail", "/platform/users/detail", 111);

        assertThatThrownBy(() -> service.updateMenu(1L, 2L, new MenuUpdateRequest(
                4L, "platform.users", "MENU", "menu.platform.users", "/platform/users",
                "component", "UserOutlined", 110, true, true
        ))).isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("COMMON_BAD_REQUEST");
    }

    @Test
    void createMenuRejectsDuplicateCodeAndMissingParent() {
        assertThatThrownBy(() -> service.createMenu(1L, new MenuCreateRequest(
                null, "platform.users", "MENU", "menu.users", "/platform/users",
                "component", "UserOutlined", 100, true, true
        ))).isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_PERMISSION_MENU_CODE_ALREADY_EXISTS");

        assertThatThrownBy(() -> service.createMenu(1L, new MenuCreateRequest(
                9_999L, "platform.ghost", "MENU", "menu.ghost", "/ghost",
                "component", "MenuOutlined", 100, true, true
        ))).isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode().code())
                .isEqualTo("PLATFORM_PERMISSION_MENU_NOT_FOUND");
    }

    private static final class InMemoryMenuRepository implements MenuRepository {

        private final AtomicLong nextId = new AtomicLong(10);
        private final Map<Long, Menu> menus = new ConcurrentHashMap<>();
        private final Map<String, Long> codesToId = new ConcurrentHashMap<>();
        private final Map<Long, List<Long>> visibleMenuIdsByUser = new HashMap<>();
        private final Map<Long, List<String>> permissionCodesByMenu = new HashMap<>();

        void seed(long id, Long parentId, String code, String routePath, int sortNo) {
            seed(id, parentId, code, routePath, sortNo, 1L);
        }

        void seed(long id, Long parentId, String code, String routePath, int sortNo, long tenantId) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            menus.put(id, new Menu(id, tenantId, parentId, code, routePath == null ? "GROUP" : "MENU",
                    "menu." + code, routePath, code, "MenuOutlined", sortNo, true, true, 0, now, now));
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
            return menus.values().stream()
                    .filter(menu -> menu.tenantId() == tenantId)
                    .sorted(Comparator.comparing(Menu::sortNo))
                    .toList();
        }

        @Override
        public List<Menu> findAllByCodes(long tenantId, Collection<String> menuCodes) {
            return List.of();
        }

        @Override
        public List<Menu> findVisibleByUserId(long tenantId, long userId, Collection<String> permissionCodes) {
            List<Long> ids = visibleMenuIdsByUser.getOrDefault(userId, List.of());
            if (ids.isEmpty()) return findAll(tenantId);
            List<Menu> result = new ArrayList<>();
            ids.forEach(id -> findById(tenantId, id)
                    .filter(menu -> {
                        List<String> required = permissionCodesByMenu.getOrDefault(menu.id(), List.of());
                        return required.isEmpty() || required.stream().anyMatch(permissionCodes::contains);
                    })
                    .ifPresent(result::add));
            return result;
        }

        @Override
        public Map<Long, List<String>> permissionCodesByMenuIds(long tenantId, Collection<Long> menuIds) {
            Map<Long, List<String>> result = new HashMap<>();
            for (Long menuId : menuIds) {
                result.put(menuId, permissionCodesByMenu.getOrDefault(menuId, List.of()));
            }
            return result;
        }

        @Override
        public List<String> findMenuCodesByRoleId(long tenantId, long roleId) {
            return List.of();
        }

        @Override
        public long insert(long operatorUserId, Menu menu) {
            long id = nextId.getAndIncrement();
            menus.put(id, new Menu(id, menu.tenantId(), menu.parentId(), menu.menuCode(), menu.menuType(),
                    menu.titleKey(), menu.routePath(), menu.componentKey(), menu.icon(), menu.sortNo(),
                    menu.visible(), menu.enabled(), 0, menu.createdAt(), menu.updatedAt()));
            codesToId.put(menu.menuCode(), id);
            return id;
        }

        @Override
        public boolean update(long operatorUserId, Menu menu) {
            menus.put(menu.id(), menu);
            return true;
        }

        @Override
        public boolean replaceRoleMenus(long operatorUserId, long tenantId, long roleId, List<Long> menuIds) {
            return true;
        }
    }
}

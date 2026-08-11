package com.company.iaf.platform.permission.application;

import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.platform.permission.domain.model.Menu;
import com.company.iaf.platform.permission.domain.repository.MenuRepository;
import com.company.iaf.platform.permission.interfaces.dto.MenuCreateRequest;
import com.company.iaf.platform.permission.interfaces.dto.MenuResponse;
import com.company.iaf.platform.permission.interfaces.dto.MenuUpdateRequest;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MenuApplicationService {

    private final MenuRepository menuRepository;

    public MenuApplicationService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @RequiresPermission("platform:menu:view")
    @Transactional(readOnly = true)
    public List<MenuResponse> listMenuTree(long tenantId) {
        return toTree(menuRepository.findAll(tenantId), tenantId);
    }

    @RequiresPermission("platform:auth:me")
    @Transactional(readOnly = true)
    public List<MenuResponse> listCurrentUserMenus(long tenantId) {
        long userId = SecurityContext.getUserId()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "User context is missing"));
        return toTree(menuRepository.findVisibleByUserId(tenantId, userId, SecurityContext.getPermissions()), tenantId);
    }

    @RequiresPermission("platform:menu:create")
    @Transactional
    public MenuResponse createMenu(long tenantId, MenuCreateRequest request) {
        if (menuRepository.existsByMenuCode(tenantId, request.menuCode())) {
            throw new BusinessException(PlatformPermissionErrorCode.MENU_CODE_ALREADY_EXISTS);
        }
        validateParent(tenantId, request.parentId());
        long currentUserId = SecurityContext.getUserId().orElse(0L);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Menu draft = new Menu(
                null,
                tenantId,
                request.parentId(),
                request.menuCode(),
                request.menuType(),
                request.titleKey(),
                request.routePath(),
                request.componentKey(),
                request.icon(),
                request.sortNo() == null ? 0 : request.sortNo(),
                request.visible(),
                request.enabled(),
                0,
                now,
                now
        );
        long id = menuRepository.insert(currentUserId, draft);
        Menu saved = menuRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_ERROR, "Inserted menu could not be reloaded"));
        return MenuResponse.from(saved, List.of(), List.of());
    }

    @RequiresPermission("platform:menu:update")
    @Transactional
    public MenuResponse updateMenu(long tenantId, long id, MenuUpdateRequest request) {
        Menu existing = menuRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(PlatformPermissionErrorCode.MENU_NOT_FOUND));
        if (!existing.menuCode().equals(request.menuCode())
                && menuRepository.existsByMenuCode(tenantId, request.menuCode())) {
            throw new BusinessException(PlatformPermissionErrorCode.MENU_CODE_ALREADY_EXISTS);
        }
        if (request.parentId() != null && request.parentId().equals(id)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Menu cannot be its own parent");
        }
        validateParent(tenantId, request.parentId());
        validateParentIsNotDescendant(tenantId, id, request.parentId());
        Menu updated = new Menu(
                existing.id(),
                existing.tenantId(),
                request.parentId(),
                request.menuCode(),
                request.menuType(),
                request.titleKey(),
                request.routePath(),
                request.componentKey(),
                request.icon(),
                request.sortNo() == null ? 0 : request.sortNo(),
                request.visible(),
                request.enabled(),
                existing.version(),
                existing.createdAt(),
                existing.updatedAt()
        );
        long currentUserId = SecurityContext.getUserId().orElse(0L);
        if (!menuRepository.update(currentUserId, updated)) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Menu was modified concurrently");
        }
        Menu saved = menuRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(PlatformPermissionErrorCode.MENU_NOT_FOUND));
        return MenuResponse.from(saved, List.of(), List.of());
    }

    private void validateParent(long tenantId, Long parentId) {
        if (parentId != null && menuRepository.findById(tenantId, parentId).isEmpty()) {
            throw new BusinessException(PlatformPermissionErrorCode.MENU_NOT_FOUND);
        }
    }

    private void validateParentIsNotDescendant(long tenantId, long menuId, Long parentId) {
        if (parentId == null) {
            return;
        }
        Set<Long> descendants = descendantIds(menuId, menuRepository.findAll(tenantId));
        if (descendants.contains(parentId)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "Menu cannot use its descendant as parent");
        }
    }

    private Set<Long> descendantIds(long menuId, List<Menu> menus) {
        Map<Long, List<Menu>> childrenByParent = new HashMap<>();
        for (Menu menu : menus) {
            if (menu.parentId() != null) {
                childrenByParent.computeIfAbsent(menu.parentId(), ignored -> new ArrayList<>()).add(menu);
            }
        }
        Set<Long> descendants = new java.util.HashSet<>();
        collectDescendants(menuId, childrenByParent, descendants);
        return descendants;
    }

    private void collectDescendants(long menuId, Map<Long, List<Menu>> childrenByParent, Set<Long> descendants) {
        for (Menu child : childrenByParent.getOrDefault(menuId, List.of())) {
            if (descendants.add(child.id())) {
                collectDescendants(child.id(), childrenByParent, descendants);
            }
        }
    }

    private List<MenuResponse> toTree(List<Menu> menus, long tenantId) {
        List<Long> ids = menus.stream().map(Menu::id).toList();
        Map<Long, List<String>> permissionsByMenuId = menuRepository.permissionCodesByMenuIds(tenantId, ids);
        Map<Long, List<Menu>> childrenByParent = new LinkedHashMap<>();
        List<Menu> roots = new ArrayList<>();
        Map<Long, Menu> menuById = new HashMap<>();
        menus.forEach(menu -> menuById.put(menu.id(), menu));
        for (Menu menu : menus) {
            if (menu.parentId() == null || !menuById.containsKey(menu.parentId())) {
                roots.add(menu);
            } else {
                childrenByParent.computeIfAbsent(menu.parentId(), ignored -> new ArrayList<>()).add(menu);
            }
        }
        Comparator<Menu> sort = Comparator.comparing(Menu::sortNo).thenComparing(Menu::id);
        roots.sort(sort);
        childrenByParent.values().forEach(children -> children.sort(sort));
        return roots.stream()
                .map(menu -> toNode(menu, childrenByParent, permissionsByMenuId))
                .toList();
    }

    private MenuResponse toNode(
            Menu menu,
            Map<Long, List<Menu>> childrenByParent,
            Map<Long, List<String>> permissionsByMenuId
    ) {
        List<MenuResponse> children = childrenByParent.getOrDefault(menu.id(), List.of()).stream()
                .map(child -> toNode(child, childrenByParent, permissionsByMenuId))
                .toList();
        return MenuResponse.from(menu, permissionsByMenuId.getOrDefault(menu.id(), List.of()), children);
    }
}

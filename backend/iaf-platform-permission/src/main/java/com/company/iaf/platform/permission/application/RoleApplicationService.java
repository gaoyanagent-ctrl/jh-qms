package com.company.iaf.platform.permission.application;

import com.company.iaf.platform.permission.domain.model.Permission;
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
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.result.PageResult;
import com.company.iaf.shared.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Application service for platform role and permission assignment.
 * Role CRUD uses {@link RoleRepository}; permission validation and the
 * assignment flow rely on {@link PermissionRepository}.
 */
@Service
public class RoleApplicationService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;

    public RoleApplicationService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            MenuRepository menuRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.menuRepository = menuRepository;
    }

    @com.company.iaf.platform.core.security.RequiresPermission("platform:role:view")
    @Transactional(readOnly = true)
    public PageResult<RoleResponse> listRoles(long tenantId, String keyword, long pageNo, long pageSize) {
        long safePage = Math.max(1, pageNo);
        long safeSize = pageSize <= 0 ? 20 : Math.min(pageSize, 200);
        long total = roleRepository.count(tenantId, keyword);
        if (total == 0) {
            return PageResult.empty(safePage, safeSize);
        }
        List<Role> roles = roleRepository.findPage(tenantId, keyword, (int) safePage, (int) safeSize);
        List<RoleResponse> records = roles.stream()
                .map(role -> RoleResponse.from(role, permissionCodes(role.id(), tenantId), menuCodes(role.id(), tenantId)))
                .toList();
        return new PageResult<>(records, total, safePage, safeSize);
    }

    @com.company.iaf.platform.core.security.RequiresPermission("platform:role:view")
    @Transactional(readOnly = true)
    public RoleResponse getRole(long tenantId, long id) {
        Role role = roleRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(PlatformPermissionErrorCode.ROLE_NOT_FOUND));
        return RoleResponse.from(role, permissionCodes(id, tenantId), menuCodes(id, tenantId));
    }

    @com.company.iaf.platform.core.security.RequiresPermission("platform:role:create")
    @Transactional
    public RoleResponse createRole(long tenantId, RoleCreateRequest request) {
        if (roleRepository.existsByRoleCode(tenantId, request.roleCode())) {
            throw new BusinessException(PlatformPermissionErrorCode.ROLE_CODE_ALREADY_EXISTS);
        }
        long currentUserId = SecurityContext.getUserId().orElse(0L);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Role draft = new Role(
                null,
                tenantId,
                request.roleCode(),
                request.roleName(),
                request.roleType(),
                request.status() == null ? RoleStatus.ENABLED : request.status(),
                0,
                now,
                now
        );
        long id = roleRepository.insert(currentUserId, draft);
        Role saved = roleRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_ERROR, "Inserted role could not be reloaded"));
        return RoleResponse.from(saved, List.of(), List.of());
    }

    @com.company.iaf.platform.core.security.RequiresPermission("platform:role:update")
    @Transactional
    public RoleResponse updateRole(long tenantId, long id, RoleUpdateRequest request) {
        Role existing = roleRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(PlatformPermissionErrorCode.ROLE_NOT_FOUND));
        if (!existing.roleCode().equals(request.roleCode())
                && roleRepository.existsByRoleCode(tenantId, request.roleCode())) {
            throw new BusinessException(PlatformPermissionErrorCode.ROLE_CODE_ALREADY_EXISTS);
        }
        Role updated = new Role(
                existing.id(),
                existing.tenantId(),
                request.roleCode(),
                request.roleName(),
                request.roleType(),
                request.status(),
                existing.version(),
                existing.createdAt(),
                existing.updatedAt()
        );
        long currentUserId = SecurityContext.getUserId().orElse(0L);
        if (!roleRepository.update(currentUserId, updated)) {
            throw new BusinessException(CommonErrorCode.CONFLICT, "Role was modified concurrently");
        }
        Role reloaded = roleRepository.findById(tenantId, id)
                .orElseThrow(() -> new BusinessException(PlatformPermissionErrorCode.ROLE_NOT_FOUND));
        return RoleResponse.from(reloaded, permissionCodes(id, tenantId), menuCodes(id, tenantId));
    }

    @com.company.iaf.platform.core.security.RequiresPermission("platform:role:assign-permission")
    @Transactional
    public RoleResponse assignPermissions(long tenantId, long roleId, AssignRolePermissionsRequest request) {
        roleRepository.findById(tenantId, roleId)
                .orElseThrow(() -> new BusinessException(PlatformPermissionErrorCode.ROLE_NOT_FOUND));
        // Deduplicate and preserve caller order while keeping the result
        // easy to diff against future requests.
        Set<String> requested = new LinkedHashSet<>(request.permissionCodes());
        List<Long> permissionIds = List.of();
        if (!requested.isEmpty()) {
            List<Permission> found = permissionRepository.findAllByCodes(tenantId, requested);
            if (found.size() != requested.size()) {
                Set<String> foundCodes = new HashSet<>();
                for (Permission permission : found) {
                    foundCodes.add(permission.permissionCode());
                }
                Set<String> missing = new TreeSet<>(requested);
                missing.removeAll(foundCodes);
                throw new BusinessException(
                        PlatformPermissionErrorCode.PERMISSION_NOT_FOUND,
                        "Unknown permission codes: " + missing);
            }
            permissionIds = found.stream().map(Permission::id).toList();
        }
        long currentUserId = SecurityContext.getUserId().orElse(0L);
        if (!roleRepository.replacePermissions(currentUserId, tenantId, roleId, permissionIds)) {
            throw new BusinessException(PlatformPermissionErrorCode.ROLE_NOT_FOUND);
        }
        Role saved = roleRepository.findById(tenantId, roleId)
                .orElseThrow(() -> new BusinessException(PlatformPermissionErrorCode.ROLE_NOT_FOUND));
        return RoleResponse.from(saved, permissionCodes(roleId, tenantId), menuCodes(roleId, tenantId));
    }

    @com.company.iaf.platform.core.security.RequiresPermission("platform:role:assign-menu")
    @Transactional
    public RoleResponse assignMenus(long tenantId, long roleId, AssignRoleMenusRequest request) {
        roleRepository.findById(tenantId, roleId)
                .orElseThrow(() -> new BusinessException(PlatformPermissionErrorCode.ROLE_NOT_FOUND));
        Set<String> requested = new LinkedHashSet<>(request.menuCodes());
        List<Long> menuIds = List.of();
        if (!requested.isEmpty()) {
            var found = menuRepository.findAllByCodes(tenantId, requested);
            if (found.size() != requested.size()) {
                Set<String> foundCodes = new HashSet<>();
                found.forEach(menu -> foundCodes.add(menu.menuCode()));
                Set<String> missing = new TreeSet<>(requested);
                missing.removeAll(foundCodes);
                throw new BusinessException(
                        PlatformPermissionErrorCode.MENU_NOT_FOUND,
                        "Unknown menu codes: " + missing);
            }
            menuIds = found.stream().map(menu -> menu.id()).toList();
        }
        long currentUserId = SecurityContext.getUserId().orElse(0L);
        if (!menuRepository.replaceRoleMenus(currentUserId, tenantId, roleId, menuIds)) {
            throw new BusinessException(PlatformPermissionErrorCode.ROLE_NOT_FOUND);
        }
        Role saved = roleRepository.findById(tenantId, roleId)
                .orElseThrow(() -> new BusinessException(PlatformPermissionErrorCode.ROLE_NOT_FOUND));
        return RoleResponse.from(saved, permissionCodes(roleId, tenantId), menuCodes(roleId, tenantId));
    }

    private List<String> permissionCodes(long roleId, long tenantId) {
        return permissionRepository.findAllByRoleId(tenantId, roleId).stream()
                .map(Permission::permissionCode)
                .toList();
    }

    private List<String> menuCodes(long roleId, long tenantId) {
        return menuRepository.findMenuCodesByRoleId(tenantId, roleId);
    }
}

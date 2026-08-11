package com.company.iaf.platform.permission.domain.repository;

import com.company.iaf.platform.permission.domain.model.Menu;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistence boundary for platform menu metadata and role-menu bindings.
 */
public interface MenuRepository {

    Optional<Menu> findById(long tenantId, long id);

    boolean existsByMenuCode(long tenantId, String menuCode);

    List<Menu> findAll(long tenantId);

    List<Menu> findAllByCodes(long tenantId, Collection<String> menuCodes);

    List<Menu> findVisibleByUserId(long tenantId, long userId, Collection<String> permissionCodes);

    Map<Long, List<String>> permissionCodesByMenuIds(long tenantId, Collection<Long> menuIds);

    List<String> findMenuCodesByRoleId(long tenantId, long roleId);

    long insert(long operatorUserId, Menu menu);

    boolean update(long operatorUserId, Menu menu);

    boolean replaceRoleMenus(long operatorUserId, long tenantId, long roleId, List<Long> menuIds);
}

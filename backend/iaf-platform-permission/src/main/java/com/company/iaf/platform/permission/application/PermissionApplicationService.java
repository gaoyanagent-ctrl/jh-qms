package com.company.iaf.platform.permission.application;

import com.company.iaf.platform.core.security.RequiresPermission;
import com.company.iaf.platform.permission.domain.repository.PermissionRepository;
import com.company.iaf.platform.permission.interfaces.dto.PermissionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionApplicationService {

    private final PermissionRepository permissionRepository;

    public PermissionApplicationService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @RequiresPermission("platform:permission:view")
    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions(long tenantId) {
        return permissionRepository.findAll(tenantId).stream()
                .map(PermissionResponse::from)
                .toList();
    }
}

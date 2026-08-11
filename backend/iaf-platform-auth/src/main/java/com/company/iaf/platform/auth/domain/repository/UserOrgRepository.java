package com.company.iaf.platform.auth.domain.repository;

import com.company.iaf.platform.auth.domain.model.UserOrg;
import com.company.iaf.platform.auth.domain.model.UserOrgAssignment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserOrgRepository {

    List<UserOrg> findByUserId(long tenantId, long userId);

    Optional<UserOrg> findByUserAndOrgId(long tenantId, long userId, long orgId);

    boolean allOrgsExist(long tenantId, Collection<Long> orgIds);

    void replaceUserOrgs(long operatorUserId, long tenantId, long userId, List<UserOrgAssignment> assignments);

    void updateUserPrimaryOrg(long operatorUserId, long tenantId, long userId, Long primaryOrgId);
}

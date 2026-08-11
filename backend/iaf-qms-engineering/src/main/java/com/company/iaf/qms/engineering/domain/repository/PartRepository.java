package com.company.iaf.qms.engineering.domain.repository;

import com.company.iaf.qms.engineering.domain.model.Part;

import java.util.List;
import java.util.Optional;

public interface PartRepository {
    Optional<Part> findById(long tenantId, long orgId, long id);
    boolean existsByPartNo(long tenantId, long orgId, String partNo);
    List<Part> findPage(long tenantId, long orgId, String keyword, long offset, int pageSize);
    long count(long tenantId, long orgId, String keyword);
    long insert(long operatorUserId, Part part);
}

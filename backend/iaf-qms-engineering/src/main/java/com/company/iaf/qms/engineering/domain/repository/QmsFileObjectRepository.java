package com.company.iaf.qms.engineering.domain.repository;

import com.company.iaf.qms.engineering.domain.model.QmsFileObject;
import java.util.Optional;

public interface QmsFileObjectRepository {
    long insert(long actorId, QmsFileObject file);
    Optional<QmsFileObject> findById(long tenantId, long orgId, long id);
}


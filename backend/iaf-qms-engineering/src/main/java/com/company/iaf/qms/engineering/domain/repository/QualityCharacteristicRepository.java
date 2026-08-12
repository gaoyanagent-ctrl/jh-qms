package com.company.iaf.qms.engineering.domain.repository;

import com.company.iaf.qms.engineering.domain.model.QualityCharacteristic;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface QualityCharacteristicRepository {
    void generateDimensionCandidates(long actorId, long tenantId, long orgId, long revisionId);
    List<QualityCharacteristic> findByRevision(long tenantId, long orgId, long revisionId);
    Optional<QualityCharacteristic> findById(long tenantId, long orgId, long revisionId, long id);
    boolean review(long actorId, long tenantId, long orgId, long revisionId, long id, int version,
                   String reviewStatus, String name, BigDecimal nominalValue, BigDecimal upperTolerance,
                   BigDecimal lowerTolerance, String unit, String comment);
}

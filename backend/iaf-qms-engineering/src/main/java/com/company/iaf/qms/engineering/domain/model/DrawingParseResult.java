package com.company.iaf.qms.engineering.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record DrawingParseResult(String schemaVersion, String documentId, String revisionCode,
                                 JsonNode modelJson, List<DrawingEntity> entities,
                                 List<SourceEvidence> evidence) {
    public DrawingParseResult {
        entities = entities == null ? List.of() : List.copyOf(entities);
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
    }
}

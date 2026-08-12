package com.company.iaf.qms.engineering.support;

import com.company.iaf.qms.engineering.application.CadParserPort;
import com.company.iaf.qms.engineering.domain.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.List;

public final class MockCadParserAdapter implements CadParserPort {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public DrawingParseResult parse(byte[] content, String fileName, String documentId, String revisionCode) {
        if (content.length < 4 || content[0] != 'A' || content[1] != 'C'
                || content[2] != '1' || content[3] != '0') {
            throw new IllegalArgumentException("Invalid DWG signature");
        }
        String entityId = "dwg-handle-2A";
        String evidenceKey = "ev-" + entityId;
        ObjectNode bbox = mapper.createObjectNode().put("x", 10).put("y", 20).put("width", 40).put("height", 12);
        ObjectNode entityJson = mapper.createObjectNode()
                .put("entityId", entityId).put("sourceEntityHandle", "2A")
                .put("entityType", "DIMENSION").put("sheetNo", "1")
                .set("bbox", bbox);
        entityJson.put("rawText", "8±0.5").put("normalizedText", "8±0.5");
        entityJson.putArray("evidence").addObject().put("evidenceKey", evidenceKey);
        ObjectNode sheet = mapper.createObjectNode().put("sheetNo", "1").put("width", 420).put("height", 297);
        sheet.set("titleBlock", mapper.createObjectNode()); sheet.putArray("views");
        sheet.putArray("entities").add(entityJson); sheet.putArray("notes"); sheet.putArray("characteristicCandidates");
        ObjectNode model = mapper.createObjectNode().put("schemaVersion", "1.0.0")
                .put("documentId", documentId).put("revision", revisionCode);
        model.putArray("sheets").add(sheet);
        DrawingEntity entity = new DrawingEntity(null, 0, 0, 0, 0, entityId, "2A",
                DrawingEntityType.DIMENSION, "DIM", "1", bd(10), bd(20), bd(40), bd(12),
                null, "8±0.5", "8±0.5", null, 0, null, null);
        SourceEvidence evidence = new SourceEvidence(null, 0, 0, 0, 0, 0, evidenceKey,
                entityId, "2A", "1", null, bd(10), bd(20), bd(40), bd(12), "8±0.5",
                "8±0.5", EvidenceExtractorType.DWG_ENTITY, "mock-cad-1.0.0", null, null,
                BigDecimal.ONE, 0, null, null);
        return new DrawingParseResult("1.0.0", documentId, revisionCode, model,
                List.of(entity), List.of(evidence));
    }

    @Override public String providerId() { return "mock-cad"; }
    private static BigDecimal bd(long value) { return BigDecimal.valueOf(value); }
}

package com.company.iaf.qms.engineering.infrastructure.parser;

import com.company.iaf.qms.engineering.application.CadParserPort;
import com.company.iaf.qms.engineering.domain.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnProperty(name = "qms.cad.provider", havingValue = "libredwg")
public class HttpLibreDwgCadParserAdapter implements CadParserPort {
    private final RestClient client;

    public HttpLibreDwgCadParserAdapter(RestClient.Builder builder,
            @Value("${qms.parser.base-url:http://localhost:18083}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    @Override
    public DrawingParseResult parse(byte[] content, String fileName, String documentId, String revisionCode) {
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("document_id", documentId);
        body.add("revision", revisionCode);
        body.add("file", new ByteArrayResource(content) {
            @Override public String getFilename() { return fileName; }
        });
        ParserResponse response = client.post().uri("/internal/v1/cad/parse")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(ParserResponse.class);
        if (response == null) throw new IllegalStateException("CAD parser returned an empty response");
        List<DrawingEntity> entities = response.entities().stream().map(value -> new DrawingEntity(
                null, 0, 0, 0, 0, value.entityId(), value.sourceEntityHandle(),
                DrawingEntityType.valueOf(value.entityType()), value.layer(), value.sheetNo(),
                decimal(value.bbox().x()), decimal(value.bbox().y()), decimal(value.bbox().width()),
                decimal(value.bbox().height()), value.geometry(), value.rawText(), value.normalizedText(),
                value.style(), 0, null, null)).toList();
        List<SourceEvidence> evidence = response.evidence().stream().map(value -> new SourceEvidence(
                null, 0, 0, 0, 0, 0, value.evidenceKey(), value.entityId(), value.entityHandle(),
                value.sheetNo(), value.pageNo(), decimal(value.bbox().x()), decimal(value.bbox().y()),
                decimal(value.bbox().width()), decimal(value.bbox().height()), value.rawText(),
                value.normalizedText(), EvidenceExtractorType.valueOf(value.extractorType()),
                value.extractorVersion(), value.modelName(), value.modelVersion(),
                decimal(value.confidence()), 0, null, null)).toList();
        return new DrawingParseResult(response.schemaVersion(), response.documentId(),
                response.revisionCode(), response.modelJson(), entities, evidence);
    }

    @Override public String providerId() { return "libredwg-0.14"; }
    private static BigDecimal decimal(double value) { return BigDecimal.valueOf(value); }
    private record Box(double x, double y, double width, double height) { }
    private record EntityPayload(String entityId, String sourceEntityHandle, String entityType,
            String layer, String sheetNo, Box bbox, JsonNode geometry, String rawText,
            String normalizedText, JsonNode style) { }
    private record EvidencePayload(String evidenceKey, String entityId, String entityHandle,
            String sheetNo, Integer pageNo, Box bbox, String rawText, String normalizedText,
            String extractorType, String extractorVersion, String modelName, String modelVersion,
            double confidence) { }
    private record ParserResponse(String schemaVersion, String documentId, String revisionCode,
            JsonNode modelJson, List<EntityPayload> entities, List<EvidencePayload> evidence) { }
}

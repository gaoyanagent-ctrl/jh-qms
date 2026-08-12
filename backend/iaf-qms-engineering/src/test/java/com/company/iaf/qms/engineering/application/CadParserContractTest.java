package com.company.iaf.qms.engineering.application;

import com.company.iaf.qms.engineering.domain.model.DrawingEntityType;
import com.company.iaf.qms.engineering.domain.model.EvidenceExtractorType;
import com.company.iaf.qms.engineering.support.MockCadParserAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CadParserContractTest {
    @Test
    void providerPreservesHandlesAndEmitsTheSharedDimContract() {
        CadParserPort provider = new MockCadParserAdapter();
        var result = provider.parse("AC1027 MOCK".getBytes(), "drawing.dwg", "drawing-8", "B");

        assertThat(provider.providerId()).isEqualTo("mock-cad");
        assertThat(result.schemaVersion()).isEqualTo("1.0.0");
        assertThat(result.modelJson().path("sheets").get(0).path("entities").get(0)
                .path("sourceEntityHandle").asText()).isEqualTo("2A");
        assertThat(result.entities()).singleElement().satisfies(entity -> {
            assertThat(entity.entityType()).isEqualTo(DrawingEntityType.DIMENSION);
            assertThat(entity.sourceEntityHandle()).isEqualTo("2A");
        });
        assertThat(result.evidence()).singleElement().satisfies(evidence -> {
            assertThat(evidence.extractorType()).isEqualTo(EvidenceExtractorType.DWG_ENTITY);
            assertThat(evidence.entityHandle()).isEqualTo("2A");
        });
    }
}

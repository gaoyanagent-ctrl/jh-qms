package com.company.iaf.qms.engineering.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrawingRevisionTest {

    @Test
    void metadataDraftStartsInSafeUnpublishedState() {
        DrawingRevision revision = DrawingRevision.metadataDraft(
                1L, 10L, 20L, "Z1", 1, LocalDate.of(2026, 8, 11), null
        );

        assertThat(revision.status()).isEqualTo(DrawingRevisionStatus.DRAFT);
        assertThat(revision.parseStatus()).isEqualTo(ParseStatus.PENDING);
        assertThat(revision.reviewStatus()).isEqualTo(ReviewStatus.PENDING);
        assertThat(revision.fileId()).isNull();
        assertThat(revision.releasedAt()).isNull();
    }

    @Test
    void metadataDraftRejectsNonPositiveSequence() {
        assertThatThrownBy(() -> DrawingRevision.metadataDraft(1L, 10L, 20L, "Z1", 0, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }
}

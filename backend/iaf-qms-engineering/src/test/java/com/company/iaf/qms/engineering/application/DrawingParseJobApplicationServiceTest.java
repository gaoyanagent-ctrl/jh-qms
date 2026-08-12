package com.company.iaf.qms.engineering.application;

import com.company.iaf.platform.statemachine.application.DefaultStateMachineService;
import com.company.iaf.qms.engineering.domain.model.*;
import com.company.iaf.qms.engineering.domain.repository.*;
import com.company.iaf.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DrawingParseJobApplicationServiceTest {
    private final DrawingRevisionRepository revisions=mock(DrawingRevisionRepository.class);
    private final DrawingParseJobRepository jobs=mock(DrawingParseJobRepository.class);
    private final QmsAuditTrail audit=mock(QmsAuditTrail.class);
    private final DrawingParseJobApplicationService service=new DrawingParseJobApplicationService(
            revisions,jobs,audit,new DefaultStateMachineService());

    @Test void retriesLatestFailedJobAsNextAttempt() {
        DrawingRevision revision=new DrawingRevision(5L,1,10,3,"A",1,9L,"PDF",null,null,null,
                ParseStatus.FAILED,ReviewStatus.PENDING,DrawingRevisionStatus.FAILED,"sum",null,null,2,null,null);
        DrawingParseJob failed=new DrawingParseJob(7L,1,10,5,9,1,ParseJobStatus.FAILED,"PDF",
                "PARSER_ERROR","failed",1,OffsetDateTime.now(),OffsetDateTime.now());
        DrawingParseJob queued=new DrawingParseJob(8L,1,10,5,9,2,ParseJobStatus.QUEUED,"PDF",
                null,null,0,OffsetDateTime.now(),OffsetDateTime.now());
        when(revisions.findById(1,10,5)).thenReturn(Optional.of(revision));
        when(jobs.findLatest(1,10,5)).thenReturn(Optional.of(failed),Optional.of(queued));
        when(revisions.transitionState(anyLong(),eq(1L),eq(10L),eq(5L),eq("FAILED"),eq("UPLOADED"),eq("PENDING"),eq(2))).thenReturn(true);
        when(jobs.enqueue(anyLong(),eq(1L),eq(10L),eq(5L),eq(9L),eq("PDF"),eq(2))).thenReturn(8L);
        assertThat(service.retry(1,10,5).attemptNo()).isEqualTo(2);
        verify(audit).record(eq(1L),anyLong(),eq("DRAWING_PARSE_RETRIED"),eq("DrawingRevision"),eq(5L),any());
    }

    @Test void rejectsRetryUnlessLatestJobFailed() {
        DrawingRevision revision=DrawingRevision.metadataDraft(1,10,3,"A",1,null,null);
        when(revisions.findById(1,10,5)).thenReturn(Optional.of(revision));
        when(jobs.findLatest(1,10,5)).thenReturn(Optional.of(new DrawingParseJob(7L,1,10,5,9,1,
                ParseJobStatus.QUEUED,"PDF",null,null,0,null,null)));
        assertThatThrownBy(() -> service.retry(1,10,5)).isInstanceOf(BusinessException.class);
    }
}

package com.company.iaf.platform.workflow.application;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalRecord(long id, String businessType, long businessId, ApprovalStatus status,
                             long submittedBy, LocalDateTime submittedAt, Long decidedBy,
                             LocalDateTime decidedAt, String decisionComment, List<Action> actions) {
    public record Action(String action, long actorId, String comment, LocalDateTime actedAt) {}
}

package com.company.iaf.platform.integration.application;

import com.company.iaf.platform.integration.domain.model.OutboxEvent;

public interface OutboxEventHandler {

    boolean supports(String eventType);

    void handle(OutboxEvent event);
}

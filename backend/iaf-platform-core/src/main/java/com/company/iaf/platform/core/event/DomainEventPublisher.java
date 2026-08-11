package com.company.iaf.platform.core.event;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}

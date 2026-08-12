package com.company.iaf.platform.statemachine.application;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultStateMachineServiceTest {
    enum Status { DRAFT, UPLOADED }
    private final DefaultStateMachineService service = new DefaultStateMachineService();
    private final List<StateTransition<Status>> transitions =
            List.of(new StateTransition<>(Status.DRAFT, "upload", Status.UPLOADED));

    @Test void resolvesConfiguredTransition() {
        assertThat(service.requireTransition(Status.DRAFT, "upload", transitions).to()).isEqualTo(Status.UPLOADED);
    }
    @Test void rejectsUnconfiguredTransition() {
        assertThatThrownBy(() -> service.requireTransition(Status.UPLOADED, "upload", transitions))
                .isInstanceOf(IllegalStateException.class);
    }
}

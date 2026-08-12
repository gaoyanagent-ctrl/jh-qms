package com.company.iaf.platform.statemachine.application;

import org.springframework.stereotype.Service;
import java.util.Collection;

@Service
public class DefaultStateMachineService implements StateMachineService {
    @Override
    public <S extends Enum<S>> StateTransition<S> requireTransition(
            S currentState, String action, Collection<StateTransition<S>> transitions) {
        return transitions.stream()
                .filter(value -> value.from() == currentState && value.action().equals(action))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Invalid transition: " + currentState + " --" + action + "--> ?"));
    }
}

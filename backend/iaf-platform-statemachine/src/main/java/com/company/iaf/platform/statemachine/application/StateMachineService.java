package com.company.iaf.platform.statemachine.application;

import java.util.Collection;

public interface StateMachineService {
    <S extends Enum<S>> StateTransition<S> requireTransition(
            S currentState, String action, Collection<StateTransition<S>> transitions);
}

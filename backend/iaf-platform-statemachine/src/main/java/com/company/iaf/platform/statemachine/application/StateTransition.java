package com.company.iaf.platform.statemachine.application;

public record StateTransition<S extends Enum<S>>(S from, String action, S to) {
}

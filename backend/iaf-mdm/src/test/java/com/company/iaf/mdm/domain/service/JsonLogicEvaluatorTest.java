package com.company.iaf.mdm.domain.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class JsonLogicEvaluatorTest {
    private final JsonLogicEvaluator evaluator=new JsonLogicEvaluator();
    @Test void evaluatesAllowListedComparisonAndBooleanOperators(){
        var expression=Map.<String,Object>of("and",List.of(Map.of(">",List.of(Map.of("var","quantity"),0)),Map.of("==",List.of(Map.of("var","status"),"ACTIVE"))));
        assertThat(evaluator.matches(expression,Map.of("quantity",2,"status","ACTIVE"))).isTrue();
        assertThat(evaluator.matches(expression,Map.of("quantity",0,"status","ACTIVE"))).isFalse();
    }
    @Test void rejectsUnknownOperator(){assertThat(evaluator.matches(Map.of("java",List.of("danger")),Map.of())).isFalse();}
}

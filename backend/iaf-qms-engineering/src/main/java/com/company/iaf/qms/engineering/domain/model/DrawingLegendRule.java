package com.company.iaf.qms.engineering.domain.model;

public record DrawingLegendRule(long id,String ruleCode,String marker,String description,
        String targetField,String targetValue,String matchMode,int priority,boolean enabled,int version) { }

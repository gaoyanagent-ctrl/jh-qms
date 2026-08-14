package com.company.iaf.qms.engineering.interfaces.dto;
import jakarta.validation.constraints.Size;
public record ValidationPlanActionRequest(@Size(max=1000) String comment) {}

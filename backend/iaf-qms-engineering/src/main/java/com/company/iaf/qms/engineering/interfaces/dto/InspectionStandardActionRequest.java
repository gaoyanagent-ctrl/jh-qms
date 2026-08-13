package com.company.iaf.qms.engineering.interfaces.dto;

import jakarta.validation.constraints.Size;

public record InspectionStandardActionRequest(@Size(max = 1000) String comment) {}

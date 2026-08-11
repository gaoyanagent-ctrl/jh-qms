package com.company.iaf.app.interfaces.controller;

import com.company.iaf.shared.result.Result;
import java.time.OffsetDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @GetMapping
    public Result<HealthResponse> health() {
        return Result.ok(new HealthResponse("OK", OffsetDateTime.now()));
    }

    public record HealthResponse(String status, OffsetDateTime timestamp) {
    }
}

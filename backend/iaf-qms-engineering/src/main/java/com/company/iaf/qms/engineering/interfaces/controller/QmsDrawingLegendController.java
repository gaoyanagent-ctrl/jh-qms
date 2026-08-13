package com.company.iaf.qms.engineering.interfaces.controller;
import com.company.iaf.qms.engineering.application.DrawingLegendRuleService;
import com.company.iaf.qms.engineering.domain.model.DrawingLegendRule;
import com.company.iaf.qms.engineering.interfaces.dto.DrawingLegendRuleUpdateRequest;
import com.company.iaf.shared.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/qms/drawing-legend-rules")
public class QmsDrawingLegendController {
 private final DrawingLegendRuleService service;public QmsDrawingLegendController(DrawingLegendRuleService service){this.service=service;}
 @GetMapping public Result<List<DrawingLegendRule>> list(){return Result.ok(service.list(QmsRequestContext.tenantId()));}
 @PutMapping public Result<List<DrawingLegendRule>> update(@Valid @RequestBody DrawingLegendRuleUpdateRequest request){return Result.ok(service.update(QmsRequestContext.tenantId(),QmsRequestContext.orgId(),request));}
}

package com.company.iaf.platform.org.interfaces.controller;

import com.company.iaf.platform.org.application.OrgApplicationService;
import com.company.iaf.platform.org.application.PlatformOrgErrorCode;
import com.company.iaf.platform.org.domain.model.OrgStatus;
import com.company.iaf.platform.org.domain.model.OrgType;
import com.company.iaf.platform.org.interfaces.dto.OrgCreateRequest;
import com.company.iaf.platform.org.interfaces.dto.OrgResponse;
import com.company.iaf.platform.org.interfaces.dto.OrgTreeNodeResponse;
import com.company.iaf.platform.org.interfaces.dto.OrgUpdateRequest;
import com.company.iaf.platform.core.security.PermissionChecker;
import com.company.iaf.platform.core.security.RequiresPermissionAspect;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.result.Result;
import com.company.iaf.shared.security.SecurityContext;
import com.company.iaf.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = OrgControllerTest.TestConfig.class)
class OrgControllerTest {

    private final OrgApplicationService orgApplicationService = mock(OrgApplicationService.class);
    @Autowired
    private PermissionChecker permissionChecker;
    @Autowired
    private RequiresPermissionAspect requiresPermissionAspect;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OrgController controller = new OrgController(orgApplicationService);
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(mapper))
                .build();
        SecurityContext.setUserId(99L);
        TenantContext.setTenantId(1L);
        SecurityContext.setPermissions(Set.of(
                "platform:org:view", "platform:org:create", "platform:org:update"));
    }

    @AfterEach
    void clearContext() {
        SecurityContext.clear();
        TenantContext.clear();
    }

    @Test
    void treeReturnsEnvelopeWithNodes() throws Exception {
        when(orgApplicationService.getTree(eq(1L)))
                .thenReturn(List.of(sampleNode(1L, "ACME", null)));

        mockMvc.perform(get("/api/platform/orgs/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orgCode").value("ACME"));
    }

    @Test
    void getReturnsSingleOrgEnvelope() throws Exception {
        when(orgApplicationService.getOrg(eq(1L), eq(1L))).thenReturn(sampleOrg(1L, "ACME"));

        mockMvc.perform(get("/api/platform/orgs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orgCode").value("ACME"));
    }

    @Test
    void createReturnsCreatedOrg() throws Exception {
        when(orgApplicationService.createOrg(eq(1L), any(OrgCreateRequest.class)))
                .thenReturn(sampleOrg(2L, "ENG"));

        mockMvc.perform(post("/api/platform/orgs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orgCode\":\"ENG\",\"orgName\":\"Engineering\","
                                + "\"orgType\":\"DIVISION\",\"status\":\"ENABLED\",\"sortNo\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orgCode").value("ENG"));
    }

    @Test
    void updateReturnsUpdatedOrg() throws Exception {
        when(orgApplicationService.updateOrg(eq(1L), eq(1L), any(OrgUpdateRequest.class)))
                .thenReturn(sampleOrg(1L, "ACME-2"));

        mockMvc.perform(put("/api/platform/orgs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orgCode\":\"ACME-2\",\"orgName\":\"ACME Renamed\","
                                + "\"orgType\":\"COMPANY\",\"status\":\"ENABLED\",\"sortNo\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orgCode").value("ACME-2"));
    }

    @Test
    void createPropagatesBusinessError() throws Exception {
        when(orgApplicationService.createOrg(eq(1L), any(OrgCreateRequest.class)))
                .thenThrow(new BusinessException(PlatformOrgErrorCode.ORG_CODE_ALREADY_EXISTS));

        mockMvc.perform(post("/api/platform/orgs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orgCode\":\"ENG\",\"orgName\":\"Engineering\","
                                + "\"orgType\":\"DIVISION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLATFORM_ORG_CODE_ALREADY_EXISTS"));
    }

    private static OrgResponse sampleOrg(long id, String code) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return new OrgResponse(id, 1L, null, code, code, OrgType.COMPANY,
                OrgStatus.ENABLED, 0, 0, now, now);
    }

    private static OrgTreeNodeResponse sampleNode(long id, String code, Long parentId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OrgResponse response = new OrgResponse(id, 1L, parentId, code, code, OrgType.COMPANY,
                OrgStatus.ENABLED, 0, 0, now, now);
        return OrgTreeNodeResponse.from(new com.company.iaf.platform.org.domain.model.Org(
                id, 1L, parentId, code, code, OrgType.COMPANY, OrgStatus.ENABLED, 0, 0, now, now));
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        PermissionChecker permissionChecker() {
            return new PermissionChecker();
        }

        @Bean
        RequiresPermissionAspect requiresPermissionAspect(PermissionChecker permissionChecker) {
            return new RequiresPermissionAspect(permissionChecker);
        }
    }

    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<Result<Void>> handleBusiness(BusinessException exception) {
            return ResponseEntity.status(400).body(Result.fail(
                    exception.errorCode().code(),
                    exception.errorCode().message()));
        }
    }
}
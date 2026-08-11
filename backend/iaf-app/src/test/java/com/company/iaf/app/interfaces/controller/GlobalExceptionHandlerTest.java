package com.company.iaf.app.interfaces.controller;

import com.company.iaf.app.infrastructure.config.SecurityConfig;
import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void businessExceptionReturnsUnifiedResult() throws Exception {
        mockMvc.perform(post("/test/business-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CommonErrorCode.BAD_REQUEST.code()));
    }

    @Test
    @WithMockUser
    void unauthorizedBusinessExceptionReturns401() throws Exception {
        mockMvc.perform(post("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.UNAUTHORIZED.code()));
    }

    @Test
    @WithMockUser
    void forbiddenBusinessExceptionReturns403() throws Exception {
        mockMvc.perform(post("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    @WithMockUser
    void validationExceptionReturnsUnifiedResult() throws Exception {
        mockMvc.perform(post("/test/validation-error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CommonErrorCode.VALIDATION_FAILED.code()));
    }

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @PostMapping("/business-error")
        void businessError() {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "invalid request");
        }

        @PostMapping("/unauthorized")
        void unauthorized() {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED, "missing or invalid token");
        }

        @PostMapping("/forbidden")
        void forbidden() {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "missing required permission");
        }

        @PostMapping("/validation-error")
        void validationError(@Valid @RequestBody TestRequest request) {
        }
    }

    record TestRequest(@NotBlank String name) {
    }
}

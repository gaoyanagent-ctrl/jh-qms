package com.company.iaf.app.interfaces.controller;

import com.company.iaf.shared.exception.BusinessException;
import com.company.iaf.shared.exception.CommonErrorCode;
import com.company.iaf.shared.exception.ErrorCode;
import com.company.iaf.shared.result.Result;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(httpStatusFor(exception.errorCode()))
                .body(Result.fail(exception.errorCode().code(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(Result.fail(CommonErrorCode.VALIDATION_FAILED.code(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception exception) {
        LOGGER.error("Unhandled request exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(CommonErrorCode.INTERNAL_ERROR.code(), CommonErrorCode.INTERNAL_ERROR.message()));
    }

    /**
     * Map stable business error codes to HTTP status. Unauthenticated and
     * forbidden cases must surface as 401 / 403 so HTTP-aware clients
     * (browsers, gateway rules) can react correctly; the rest stay on 400
     * because they represent caller-side validation or business rule
     * failures rather than transport-layer concerns.
     */
    private static HttpStatus httpStatusFor(ErrorCode code) {
        if (code == CommonErrorCode.UNAUTHORIZED) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code == CommonErrorCode.FORBIDDEN) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }
}

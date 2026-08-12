package com.company.iaf.qms.engineering.infrastructure.parser;

import com.company.iaf.qms.engineering.application.CadParserPort;
import com.company.iaf.qms.engineering.domain.model.DrawingParseResult;
import org.springframework.stereotype.Component;

@Component
public class UnavailableCadParserAdapter implements CadParserPort {
    @Override
    public DrawingParseResult parse(byte[] content, String fileName, String documentId, String revisionCode) {
        throw new CadProviderUnavailableException("No production CAD provider is configured");
    }

    @Override public String providerId() { return "unavailable"; }

    public static class CadProviderUnavailableException extends RuntimeException {
        public CadProviderUnavailableException(String message) { super(message); }
    }
}

package com.company.iaf.qms.engineering.application;

import com.company.iaf.qms.engineering.domain.model.DrawingParseResult;

public interface CadParserPort {
    DrawingParseResult parse(byte[] content, String fileName, String documentId, String revisionCode);
    String providerId();
}

package com.company.iaf.qms.engineering.interfaces.dto;

import com.company.iaf.qms.engineering.domain.model.DrawingRevisionFile;
import com.company.iaf.qms.engineering.domain.model.DrawingRevisionFileRole;

public record DrawingRevisionFileResponse(DrawingRevisionFileRole role, QmsFileResponse file) {
    public static DrawingRevisionFileResponse from(DrawingRevisionFile value) {
        return new DrawingRevisionFileResponse(value.role(), QmsFileResponse.from(value.file()));
    }
}

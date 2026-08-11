package com.company.iaf.platform.org.interfaces.dto;

import com.company.iaf.platform.org.domain.model.Org;
import com.company.iaf.platform.org.domain.model.OrgStatus;
import com.company.iaf.platform.org.domain.model.OrgType;

import java.util.ArrayList;
import java.util.List;

/**
 * Hierarchical representation of an organization node. Children are
 * kept in insertion order so the tree reflects the natural sibling
 * ordering supplied by the repository.
 */
public record OrgTreeNodeResponse(
        Long id,
        Long parentId,
        String orgCode,
        String orgName,
        OrgType orgType,
        OrgStatus status,
        Integer sortNo,
        List<OrgTreeNodeResponse> children
) {

    public static OrgTreeNodeResponse from(Org org) {
        return new OrgTreeNodeResponse(
                org.id(),
                org.parentId(),
                org.orgCode(),
                org.orgName(),
                org.orgType(),
                org.status(),
                org.sortNo(),
                new ArrayList<>()
        );
    }

    public static OrgTreeNodeResponse from(Org org, List<OrgTreeNodeResponse> children) {
        return new OrgTreeNodeResponse(
                org.id(),
                org.parentId(),
                org.orgCode(),
                org.orgName(),
                org.orgType(),
                org.status(),
                org.sortNo(),
                children
        );
    }
}
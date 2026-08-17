package com.company.iaf.mdm.domain.service;

import com.company.iaf.mdm.interfaces.dto.MdmDtos;
import java.util.*;
import java.util.regex.Pattern;

public final class ModelDefinitionValidator {
    private static final Pattern CODE = Pattern.compile("^[a-z][A-Za-z0-9_]{1,63}$");
    private static final Set<String> TYPES = Set.of("STRING","TEXT","INTEGER","DECIMAL","BOOLEAN","DATE","DATETIME","ENUM","REFERENCE");

    public MdmDtos.ModelValidationResult validate(List<MdmDtos.FieldDraft> fields) {
        List<String> errors = new ArrayList<>(); List<String> warnings = new ArrayList<>(); Set<String> codes = new HashSet<>();
        if (fields.isEmpty()) errors.add("At least one custom field is required");
        for (var field : fields) {
            if (!CODE.matcher(field.code()).matches()) errors.add("Invalid field code: " + field.code());
            if (!codes.add(field.code())) errors.add("Duplicate field code: " + field.code());
            if (!TYPES.contains(field.dataType())) errors.add("Unsupported data type: " + field.dataType());
            if ("ENUM".equals(field.dataType()) && (field.enumOptions() == null || field.enumOptions().isEmpty())) errors.add("Enum options are required: " + field.code());
            if ("REFERENCE".equals(field.dataType())) {
                var config=field.referenceConfig();
                if(config==null||blank(config.targetModelCode())||blank(config.valueFieldCode())||blank(config.displayFieldCode())) errors.add("Reference model, value field and display field are required: "+field.code());
            }
            if (field.maxLength() != null && field.maxLength() <= 0) errors.add("maxLength must be positive: " + field.code());
            if (!field.listVisible() && field.searchable()) warnings.add("Searchable field is hidden from list: " + field.code());
        }
        return new MdmDtos.ModelValidationResult(errors.isEmpty(), errors, warnings);
    }
    private boolean blank(String value){return value==null||value.isBlank();}
}

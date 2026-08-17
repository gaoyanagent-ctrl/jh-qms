package com.company.iaf.mdm.domain.service;

import com.company.iaf.mdm.domain.model.MdmModels;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicRecordValidator {
    public List<String> validate(MdmModels.Model model, Map<String, Object> attributes) {
        List<String> errors = new ArrayList<>();
        for (MdmModels.Field field : model.fields()) {
            Object value = attributes.get(field.code());
            if (field.required() && (value == null || value instanceof String text && text.isBlank())) {
                errors.add(field.code() + ": required"); continue;
            }
            if (value == null) continue;
            if (!matches(field.dataType(), value)) errors.add(field.code() + ": invalid " + field.dataType());
            if (value instanceof String text && field.length() != null && text.length() > field.length()) errors.add(field.code() + ": max length " + field.length());
            if ("ENUM".equals(field.dataType()) && !field.enumOptions().contains(String.valueOf(value))) errors.add(field.code() + ": invalid option");
        }
        return errors;
    }

    private boolean matches(String type, Object value) {
        return switch (type) {
            case "STRING", "TEXT", "ENUM", "DICTIONARY", "REFERENCE", "ORGANIZATION", "USER", "AUTO_CODE" -> value instanceof String;
            case "INTEGER" -> value instanceof Integer || value instanceof Long;
            case "DECIMAL" -> value instanceof Number || parses(value, BigDecimal::new);
            case "BOOLEAN" -> value instanceof Boolean;
            case "DATE" -> value instanceof LocalDate || parses(value, LocalDate::parse);
            case "DATETIME" -> value instanceof OffsetDateTime || parses(value, OffsetDateTime::parse);
            default -> true;
        };
    }
    private boolean parses(Object value, java.util.function.Function<String, ?> parser) { try { parser.apply(String.valueOf(value)); return true; } catch (RuntimeException ex) { return false; } }
}

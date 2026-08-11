package com.company.iaf.shared.result;

import java.util.List;

public record PageResult<T>(List<T> records, long total, long pageNo, long pageSize) {

    public PageResult {
        records = records == null ? List.of() : List.copyOf(records);
    }

    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return new PageResult<>(List.of(), 0, pageNo, pageSize);
    }
}

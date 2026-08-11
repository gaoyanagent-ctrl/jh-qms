package com.company.iaf.shared.result;

public record Result<T>(boolean success, String code, String message, T data) {

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, "OK", "success", data);
    }

    public static Result<Void> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(false, code, message, null);
    }
}

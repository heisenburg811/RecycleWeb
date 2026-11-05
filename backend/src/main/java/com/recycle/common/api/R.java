package com.recycle.common.api;

/**
 * 统一响应体。
 *
 * <p>业务错误一律返回 HTTP 200 + 非 0 的 code，由前端拦截器统一处理；
 * 只有系统级异常才走 5xx。
 */
public record R<T>(int code, String message, T data) {

    public static final int OK = 0;
    public static final int BIZ_ERROR = 40000;
    public static final int SYSTEM_ERROR = 50000;

    public static <T> R<T> ok(T data) {
        return new R<>(OK, "ok", data);
    }

    public static R<Void> ok() {
        return new R<>(OK, "ok", null);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> fail(String message) {
        return new R<>(BIZ_ERROR, message, null);
    }
}

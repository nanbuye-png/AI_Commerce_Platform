package com.commerce.platform.common.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 统一响应结构
 * code: 0 = 成功, 非0 = 错误（对齐 API 设计 v1.1 错误码体系）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = null;
        return r;
    }

    public static <T> Result<T> error(String message) {
        return error(10001, message);
    }
}

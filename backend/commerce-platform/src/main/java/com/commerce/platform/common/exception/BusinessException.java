package com.commerce.platform.common.exception;

/**
 * 业务异常
 * 用于在 Service 层抛出可预知的业务错误
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public int getCode() {
        return code;
    }
}
package com.commerce.platform.order.exception;

/**
 * 管理员权限异常
 * 当非管理员用户尝试访问 Admin 接口时抛出
 */
public class AdminPermissionException extends RuntimeException {

    private final int code;

    public AdminPermissionException() {
        super("权限不足，仅管理员可执行此操作");
        this.code = 32003;
    }

    public AdminPermissionException(String message) {
        super(message);
        this.code = 32003;
    }

    public int getCode() {
        return code;
    }
}
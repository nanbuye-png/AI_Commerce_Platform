package com.commerce.platform.cart.exception;

public class InvalidCartOperationException extends RuntimeException {
    private final int code = 35003;
    public InvalidCartOperationException(String message) {
        super(message);
    }
    public int getCode() { return code; }
}
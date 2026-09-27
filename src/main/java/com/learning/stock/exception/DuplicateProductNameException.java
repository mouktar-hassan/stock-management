package com.learning.stock.exception;

public class DuplicateProductNameException extends RuntimeException {
    public DuplicateProductNameException(String message) {
        super(message);
    }

    public DuplicateProductNameException(String message, Throwable cause) {
        super(message, cause);
    }
}

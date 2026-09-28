package com.example.ngo.exception;

/** A business rule was violated (mapped to HTTP 400). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

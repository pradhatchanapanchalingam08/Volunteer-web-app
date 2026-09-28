package com.example.ngo.exception;

/** A business rule conflict such as event full or duplicate (mapped to HTTP 409). */
public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(message);
    }
}

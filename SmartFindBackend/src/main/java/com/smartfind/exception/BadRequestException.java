package com.smartfind.exception;

/**
 * Exception thrown when a client request contains invalid parameters or business rule violations.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

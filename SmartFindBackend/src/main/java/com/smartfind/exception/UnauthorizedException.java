package com.smartfind.exception;

/**
 * Exception thrown when authentication or authorization fails.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

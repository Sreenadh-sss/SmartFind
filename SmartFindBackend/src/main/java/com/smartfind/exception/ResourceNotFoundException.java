package com.smartfind.exception;

/**
 * Exception thrown when a requested resource (user, item) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

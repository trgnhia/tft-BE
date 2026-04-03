package org.example.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message, Object[] objects) {
        super(message);
    }
}

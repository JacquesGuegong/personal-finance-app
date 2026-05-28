package com.financetracker.exception;

// HTTP status is set by GlobalExceptionHandler — @ResponseStatus is not needed here.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

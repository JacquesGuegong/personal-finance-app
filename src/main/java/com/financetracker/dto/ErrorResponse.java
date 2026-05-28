package com.financetracker.dto;

import java.time.Instant;

public record ErrorResponse(int status, String message, Instant timestamp) {

    // Factory method so callers never have to think about the timestamp —
    // it is always set to the exact moment the error response is created.
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, Instant.now());
    }
}

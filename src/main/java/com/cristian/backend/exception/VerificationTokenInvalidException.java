package com.cristian.backend.exception;

public class VerificationTokenInvalidException extends RuntimeException {
    public VerificationTokenInvalidException() {
        super("Verification token invalid or expired");
    }
}


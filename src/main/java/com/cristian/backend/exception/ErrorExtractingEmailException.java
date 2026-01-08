package com.cristian.backend.exception;

public class ErrorExtractingEmailException extends RuntimeException {
    public ErrorExtractingEmailException() {
        super("Error extracting email from token");
    }
}


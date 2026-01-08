package com.cristian.backend.exception;

public class TokenEmailDoesNotMatchException extends RuntimeException {
    public TokenEmailDoesNotMatchException() {
        super("Token email does not match user email");
    }
}


package com.example.maco.infra.exception;

public class InfraException extends RuntimeException {
    public InfraException(String message) {
        super(message);
    }

    public InfraException(String message, Throwable cause) {
        super(message, cause);
    }
}

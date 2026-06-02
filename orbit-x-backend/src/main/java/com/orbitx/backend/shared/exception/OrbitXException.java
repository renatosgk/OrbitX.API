package com.orbitx.backend.shared.exception;
public class OrbitXException extends RuntimeException {
    public OrbitXException(String message) {
        super(message);
    }
    public OrbitXException(String message, Throwable cause) {
        super(message, cause);
    }
}

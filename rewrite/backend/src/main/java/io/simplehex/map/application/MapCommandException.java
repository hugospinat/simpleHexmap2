package io.simplehex.map.application;

import org.springframework.http.HttpStatus;

public class MapCommandException extends RuntimeException {
    private final HttpStatus status;

    public MapCommandException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
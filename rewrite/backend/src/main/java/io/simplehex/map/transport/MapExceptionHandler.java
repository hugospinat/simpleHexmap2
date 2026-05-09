package io.simplehex.map.transport;

import io.simplehex.map.application.MapCommandException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MapExceptionHandler {

    @ExceptionHandler(MapCommandException.class)
    public ResponseEntity<MapErrorResponse> handleMapCommandException(MapCommandException exception) {
        return ResponseEntity.status(exception.status()).body(new MapErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MapErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(new MapErrorResponse("invalid_request"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MapErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new MapErrorResponse(exception.getMessage()));
    }
}
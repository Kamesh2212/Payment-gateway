package com.kk.payment_gateway.exception;


import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyProcessedException.class)
    public ResponseEntity<Map<String,Object>> handleAlreadyProcessed(AlreadyProcessedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "AlreadyProcessed",
                "paymentRef", ex.getPaymentRef()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(f -> f.getField(), f -> f.getDefaultMessage(), (a,b)->a));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "ValidationFailed",
                "details", details
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", ex.getMessage()
        ));
    }
}

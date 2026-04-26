package com.presence.ingestion.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(WebExchangeBindException ex){

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err ->{
            String field = err instanceof FieldError fe ? fe.getField() : err.getObjectName();
            errors.put(field,err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(Map.of("status", "validation_error","errors",errors));

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleGenral(Exception ex){
      //  log.error("Unhandled: {}",ex.getMessage(),ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status","error","message","Internal server error"));
    }

}

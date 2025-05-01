//package com.journal.journalbackend.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
//        Map<String, Object> errorResponse = new HashMap<>();
//
//        // Cast HttpStatusCode to HttpStatus
//        HttpStatus status = (HttpStatus) ex.getStatusCode();
//
//        errorResponse.put("timestamp", LocalDateTime.now());
//        errorResponse.put("status", status.value());
//        errorResponse.put("error", status.getReasonPhrase()); // Now using HttpStatus to get the reason phrase
//        errorResponse.put("message", ex.getReason()); // Capture the custom message ("Tag already exists")
//        errorResponse.put("path", ""); // Optional: you can capture request URI if you want
//
//        return new ResponseEntity<>(errorResponse, status);
//    }
//}
//

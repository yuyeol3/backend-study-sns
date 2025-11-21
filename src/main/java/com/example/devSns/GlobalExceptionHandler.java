package com.example.devSns;


import com.example.devSns.dto.ErrorDto;
import com.example.devSns.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.PrintStream;
import java.util.Arrays;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger =  LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorDto> handleInvalidRequestException(InvalidRequestException e) {
        return ResponseEntity.status(400).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity.status(400).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(400).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(RequestConflictException.class)
    public ResponseEntity<ErrorDto> handleRequestConflictException(RequestConflictException e) {
        return ResponseEntity.status(409).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorDto> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity.status(401).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorDto> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity.status(403).body(new ErrorDto(e.getMessage()));
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> handleNoResource(NoResourceFoundException e) {
        // 필요하면 logger.debug로만 남기기
        // logger.debug("Static resource not found: {}", e.getResourcePath());
        return ResponseEntity.status(404).body(new ErrorDto("Not Found"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDto> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(404).body(new ErrorDto("Not Found"));
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorDto> handleMissingRequestCookieException(MissingRequestCookieException e) {
        return ResponseEntity.status(400).body(new ErrorDto("Invalid Request"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception e) {

//        for (StackTraceElement s : e.getStackTrace()) {
//            logger.error(s.toString());
//        }
        e.printStackTrace();
        logger.error(e.getMessage());
        return ResponseEntity.status(500).body(new ErrorDto("Internal Server Error"));
    }
}

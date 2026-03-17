package com.minichat.shared.error;

import com.minichat.shared.response.BaseResponse;
import com.minichat.shared.response.ResponseFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ResponseFactory.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnhandledException(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(ResponseFactory.error("INTERNAL_SERVER_ERROR", "Unexpected server error"));
    }
}


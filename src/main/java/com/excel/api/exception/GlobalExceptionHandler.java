package com.excel.api.exception;

import com.excel.api.model.ExcelResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Excel操作异常
     */
    @ExceptionHandler(ExcelOperationException.class)
    public ResponseEntity<ExcelResponse> handleExcelOperationException(ExcelOperationException e) {
        log.error("Excel操作异常: {}", e.getMessage(), e);
        
        ExcelResponse response = ExcelResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExcelResponse> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.error("参数校验失败: {}", errorMessage);
        
        ExcelResponse response = ExcelResponse.builder()
                .success(false)
                .message("参数校验失败: " + errorMessage)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExcelResponse> handleException(Exception e) {
        // 忽略 favicon.ico 的 404 错误，这是浏览器的正常请求
        String errorMessage = e.getMessage();
        if (errorMessage != null && errorMessage.contains("favicon.ico")) {
            log.debug("浏览器请求 favicon: {}", errorMessage);
        } else {
            log.error("系统异常: {}", errorMessage, e);
        }
        
        ExcelResponse response = ExcelResponse.builder()
                .success(false)
                .message("系统异常: " + errorMessage)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


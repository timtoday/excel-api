package com.excel.api.exception;

/**
 * Excel操作异常
 */
public class ExcelOperationException extends RuntimeException {
    
    public ExcelOperationException(String message) {
        super(message);
    }
    
    public ExcelOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}


package com.excel.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Excel操作请求（同时包含读写）
 */
@Data
public class ExcelOperationRequest {
    
    /**
     * Excel文件名（不含路径）
     */
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    
    /**
     * 写入操作
     */
    @Valid
    private ExcelWriteRequest writeRequest;
    
    /**
     * 读取操作
     */
    @Valid
    private ExcelReadRequest readRequest;
}


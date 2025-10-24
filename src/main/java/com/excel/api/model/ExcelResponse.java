package com.excel.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Excel操作响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 读取的数据
     */
    private List<CellResult> data;
    
    /**
     * 单元格结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CellResult {
        private String sheetName;
        private String cellAddress;
        private Object value;
        private String formula;
        private String valueType;
    }
}


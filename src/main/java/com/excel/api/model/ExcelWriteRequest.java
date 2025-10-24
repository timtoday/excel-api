package com.excel.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Excel写入请求
 */
@Data
public class ExcelWriteRequest {
    
    /**
     * Excel文件名（不含路径）
     * 在组合操作(operation)中，此字段会自动从外层传递，无需手动指定
     */
    private String fileName;
    
    /**
     * Sheet名称或索引（从0开始）
     * 如果cells中单独指定了sheetName，则此字段作为默认值
     */
    private String sheetName;
    
    /**
     * 要写入的单元格数据列表
     */
    @NotNull(message = "写入数据不能为空")
    private List<CellData> cells;
    
    /**
     * 单元格数据
     */
    @Data
    public static class CellData {
        /**
         * Sheet名称（可选）
         * 如果指定，则优先使用此值；否则使用外层的sheetName
         */
        private String sheetName;
        
        /**
         * 单元格地址（如：A1, B2, AZ27）
         */
        @NotBlank(message = "单元格地址不能为空")
        private String cellAddress;
        
        /**
         * 单元格值
         */
        private Object value;
        
        /**
         * 值类型：STRING, NUMBER, BOOLEAN, FORMULA
         */
        private String valueType = "STRING";
    }
}


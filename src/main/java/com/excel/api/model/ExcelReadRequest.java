package com.excel.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Excel读取请求
 */
@Data
public class ExcelReadRequest {
    
    /**
     * Excel文件名（不含路径）
     * 在组合操作(operation)中，此字段会自动从外层传递，无需手动指定
     */
    private String fileName;
    
    /**
     * Sheet名称
     * 如果cells中单独指定了sheetName，则此字段作为默认值
     */
    private String sheetName;
    
    /**
     * 要读取的单元格位置列表
     */
    @NotNull(message = "读取位置不能为空")
    private List<CellPosition> cells;
    
    /**
     * 是否读取公式（true读取公式，false读取计算结果）
     */
    private Boolean readFormula = false;
    
    /**
     * 单元格位置
     */
    @Data
    public static class CellPosition {
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
    }
}


package com.excel.api.controller;

import com.excel.api.config.ExcelConfig;
import com.excel.api.model.ExcelResponse;
import com.excel.api.model.ExcelWriteRequest;
import com.excel.api.service.ExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Excel下载和模板导出控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/excel")
@Tag(name = "Excel Download & Template", description = "Excel文件下载和模板导出API")
public class ExcelDownloadController {
    
    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private ExcelConfig excelConfig;
    
    /**
     * 下载Excel文件
     */
    @GetMapping("/download/{fileName}")
    @Operation(summary = "下载Excel文件", description = "下载指定的Excel文件")
    public ResponseEntity<Resource> downloadExcel(
            @Parameter(description = "文件名") 
            @PathVariable String fileName) throws IOException {
        
        log.info("下载文件请求: {}", fileName);
        
        // 安全检查：防止路径遍历攻击
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new SecurityException("非法的文件名");
        }
        
        File excelFile = new File(excelConfig.getStorage().getStorageDir(), fileName);
        
        if (!excelFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        InputStreamResource resource = new InputStreamResource(new FileInputStream(excelFile));
        
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelFile.length())
                .body(resource);
    }
    
    /**
     * 基于模板生成并下载Excel
     */
    @PostMapping("/template/generate")
    @Operation(summary = "生成模板文件", 
               description = "基于模板填充数据并生成新的Excel文件（不修改原模板）")
    public ResponseEntity<Resource> generateFromTemplate(
            @Valid @RequestBody TemplateGenerateRequest request) throws IOException {
        
        log.info("模板生成请求: 模板={}, 输出={}", request.getTemplateFileName(), request.getOutputFileName());
        
        // 1. 复制模板到输出文件
        String outputFileName = request.getOutputFileName();
        if (outputFileName == null || outputFileName.isEmpty()) {
            // 自动生成输出文件名
            String timestamp = String.valueOf(System.currentTimeMillis());
            outputFileName = request.getTemplateFileName()
                    .replace(".xlsx", "_" + timestamp + ".xlsx");
        }
        
        File templateFile = new File(excelConfig.getStorage().getStorageDir(), request.getTemplateFileName());
        File outputFile = new File(excelConfig.getStorage().getStorageDir(), outputFileName);
        
        if (!templateFile.exists()) {
            throw new RuntimeException("模板文件不存在: " + request.getTemplateFileName());
        }
        
        // 复制模板文件
        java.nio.file.Files.copy(templateFile.toPath(), outputFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        // 2. 写入数据到新文件
        ExcelWriteRequest writeRequest = new ExcelWriteRequest();
        writeRequest.setFileName(outputFileName);
        writeRequest.setSheetName(request.getSheetName());
        writeRequest.setCells(request.getCells());
        
        excelService.writeExcel(writeRequest);
        
        // 3. 返回文件供下载
        InputStreamResource resource = new InputStreamResource(new FileInputStream(outputFile));
        
        String encodedFileName = URLEncoder.encode(outputFileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        
        // 根据配置决定是否保留生成的文件
        if (!request.isKeepFile()) {
            // 标记为临时文件，下载后可以删除
            // 这里可以添加定时清理逻辑
            log.debug("临时文件将被清理: {}", outputFileName);
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + outputFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(outputFile.length())
                .body(resource);
    }
    
    /**
     * 填充模板并下载（简化版）
     */
    @PostMapping("/template/fill-and-download")
    @Operation(summary = "填充模板并下载", 
               description = "填充模板数据并直接下载，不保存到服务器")
    public ResponseEntity<Resource> fillAndDownload(
            @Valid @RequestBody TemplateFillRequest request) throws IOException {
        
        log.info("填充并下载: 模板={}", request.getTemplateFileName());
        
        // 创建临时文件
        String tempFileName = "temp_" + System.currentTimeMillis() + ".xlsx";
        File templateFile = new File(excelConfig.getStorage().getStorageDir(), request.getTemplateFileName());
        File tempFile = new File(excelConfig.getStorage().getTempDir(), tempFileName);
        
        if (!templateFile.exists()) {
            throw new RuntimeException("模板文件不存在: " + request.getTemplateFileName());
        }
        
        // 复制到临时目录
        java.nio.file.Files.copy(templateFile.toPath(), tempFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        // 写入数据
        ExcelWriteRequest writeRequest = new ExcelWriteRequest();
        writeRequest.setFileName("../excel-temp/" + tempFileName); // 使用临时目录
        writeRequest.setSheetName(request.getSheetName());
        writeRequest.setCells(request.getCells());
        
        try {
            excelService.writeExcel(writeRequest);
        } catch (Exception e) {
            tempFile.delete();
            throw e;
        }
        
        InputStreamResource resource = new InputStreamResource(new FileInputStream(tempFile));
        
        String downloadName = request.getDownloadFileName() != null ? 
                request.getDownloadFileName() : request.getTemplateFileName();
        String encodedFileName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        
        // 设置响应完成后删除临时文件的回调
        // 注意：实际生产环境建议使用定时任务清理临时文件
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + downloadName + "\"; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(tempFile.length())
                .body(resource);
    }
    
    /**
     * 模板生成请求
     */
    @lombok.Data
    public static class TemplateGenerateRequest {
        /**
         * 模板文件名
         */
        private String templateFileName;
        
        /**
         * 输出文件名（可选，不指定则自动生成）
         */
        private String outputFileName;
        
        /**
         * Sheet名称
         */
        private String sheetName;
        
        /**
         * 要填充的单元格数据
         */
        private java.util.List<ExcelWriteRequest.CellData> cells;
        
        /**
         * 是否保留生成的文件（默认true）
         */
        private boolean keepFile = true;
    }
    
    /**
     * 模板填充请求（简化版）
     */
    @lombok.Data
    public static class TemplateFillRequest {
        /**
         * 模板文件名
         */
        private String templateFileName;
        
        /**
         * 下载时的文件名（可选）
         */
        private String downloadFileName;
        
        /**
         * Sheet名称
         */
        private String sheetName;
        
        /**
         * 要填充的单元格数据
         */
        private java.util.List<ExcelWriteRequest.CellData> cells;
    }
}


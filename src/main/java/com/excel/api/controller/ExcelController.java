package com.excel.api.controller;

import com.excel.api.config.ExcelConfig;
import com.excel.api.model.ExcelOperationRequest;
import com.excel.api.model.ExcelReadRequest;
import com.excel.api.model.ExcelResponse;
import com.excel.api.model.ExcelWriteRequest;
import com.excel.api.service.ExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Excel API控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/excel")
@Tag(name = "Excel API", description = "Excel文件读写和公式计算API")
public class ExcelController {
    
    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private ExcelConfig excelConfig;
    
    /**
     * 写入Excel
     */
    @PostMapping("/write")
    @Operation(summary = "写入Excel数据", description = "向指定的Excel文件写入数据，支持字符串、数字、布尔值和公式")
    public ResponseEntity<ExcelResponse> writeExcel(
            @Valid @RequestBody ExcelWriteRequest request) {
        
        // 手动验证 fileName（因为注解验证已移除以支持组合操作）
        if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ExcelResponse.builder()
                    .success(false)
                    .message("文件名不能为空")
                    .build());
        }
        
        log.info("接收写入请求: 文件={}, Sheet={}, 单元格数={}", 
                request.getFileName(), request.getSheetName(), request.getCells().size());
        
        excelService.writeExcel(request);
        
        return ResponseEntity.ok(ExcelResponse.builder()
                .success(true)
                .message("写入成功")
                .build());
    }
    
    /**
     * 读取Excel
     */
    @PostMapping("/read")
    @Operation(summary = "读取Excel数据", description = "从指定的Excel文件读取数据，支持读取公式或计算结果")
    public ResponseEntity<ExcelResponse> readExcel(
            @Valid @RequestBody ExcelReadRequest request) {
        
        // 手动验证 fileName（因为注解验证已移除以支持组合操作）
        if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ExcelResponse.builder()
                    .success(false)
                    .message("文件名不能为空")
                    .build());
        }
        
        log.info("接收读取请求: 文件={}, Sheet={}, 单元格数={}, 读取公式={}", 
                request.getFileName(), request.getSheetName(), 
                request.getCells().size(), request.getReadFormula());
        
        ExcelResponse response = excelService.readExcel(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 同时执行写入和读取操作
     */
    @PostMapping("/operation")
    @Operation(summary = "执行Excel操作", description = "先写入数据，然后读取指定单元格的值（包括公式计算结果）")
    public ResponseEntity<ExcelResponse> performOperation(
            @Valid @RequestBody ExcelOperationRequest request) {
        
        log.info("接收操作请求: 文件={}", request.getFileName());
        
        // 自动将外层的fileName传递给内部的writeRequest和readRequest
        if (request.getWriteRequest() != null && request.getWriteRequest().getFileName() == null) {
            request.getWriteRequest().setFileName(request.getFileName());
        }
        if (request.getReadRequest() != null && request.getReadRequest().getFileName() == null) {
            request.getReadRequest().setFileName(request.getFileName());
        }
        
        ExcelResponse response = excelService.writeAndRead(
                request.getWriteRequest(), 
                request.getReadRequest()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 上传Excel文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传Excel文件", description = "上传Excel文件到服务器")
    public ResponseEntity<ExcelResponse> uploadExcel(
            @Parameter(description = "Excel文件") 
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "保存的文件名（可选，默认使用原文件名）") 
            @RequestParam(value = "fileName", required = false) String fileName) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ExcelResponse.builder()
                    .success(false)
                    .message("文件不能为空")
                    .build());
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(ExcelResponse.builder()
                    .success(false)
                    .message("只支持Excel文件（.xlsx或.xls）")
                    .build());
        }
        
        try {
            String targetFileName = (fileName != null && !fileName.isEmpty()) ? fileName : originalFilename;
            
            // 确保存储目录存在
            File storageDir = excelConfig.getStorage().getStorageDir();
            if (!storageDir.exists()) {
                log.info("存储目录不存在，正在创建: {}", storageDir.getAbsolutePath());
                if (!storageDir.mkdirs()) {
                    throw new IOException("无法创建存储目录: " + storageDir.getAbsolutePath());
                }
            }
            
            log.info("存储目录路径: {}", storageDir.getAbsolutePath());
            
            // 使用绝对路径！file.transferTo() 需要绝对路径才能正确保存
            File targetFile = new File(storageDir.getAbsolutePath(), targetFileName);
            log.info("目标文件路径（绝对路径）: {}", targetFile.getAbsolutePath());
            
            // 确保父目录存在
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                log.info("父目录不存在，正在创建: {}", parentDir.getAbsolutePath());
                if (!parentDir.mkdirs()) {
                    throw new IOException("无法创建父目录: " + parentDir.getAbsolutePath());
                }
            }
            
            // 保存文件（使用绝对路径）
            file.transferTo(targetFile.getAbsoluteFile());
            
            log.info("文件上传成功: {} -> {}", targetFileName, targetFile.getAbsolutePath());
            
            return ResponseEntity.ok(ExcelResponse.builder()
                    .success(true)
                    .message("上传成功: " + targetFileName)
                    .build());
                    
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError().body(ExcelResponse.builder()
                    .success(false)
                    .message("上传失败: " + e.getMessage())
                    .build());
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    public ResponseEntity<ExcelResponse> health() {
        return ResponseEntity.ok(ExcelResponse.builder()
                .success(true)
                .message("服务正常运行")
                .build());
    }
}


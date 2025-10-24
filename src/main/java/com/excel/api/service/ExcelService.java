package com.excel.api.service;

import com.excel.api.config.ExcelConfig;
import com.excel.api.exception.ExcelOperationException;
import com.excel.api.lock.ExcelLockManager;
import com.excel.api.model.ExcelReadRequest;
import com.excel.api.model.ExcelResponse;
import com.excel.api.model.ExcelWriteRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel操作核心服务
 */
@Slf4j
@Service
public class ExcelService {
    
    @Autowired
    private ExcelConfig excelConfig;
    
    @Autowired
    private ExcelLockManager lockManager;
    
    /**
     * 写入Excel数据
     */
    public void writeExcel(ExcelWriteRequest request) {
        String fileName = request.getFileName();
        boolean lockAcquired = false;
        
        try {
            // 获取写锁
            lockAcquired = lockManager.tryAcquireWriteLock(
                    fileName, 
                    excelConfig.getLock().getWaitTime()
            );
            
            if (!lockAcquired) {
                throw new ExcelOperationException("无法获取文件写锁，请稍后重试: " + fileName);
            }
            
            File excelFile = getExcelFile(fileName);
            Workbook workbook = null;
            
            try {
                // 打开或创建workbook
                if (excelFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(excelFile)) {
                        workbook = new XSSFWorkbook(fis);
                    }
                } else {
                    workbook = new XSSFWorkbook();
                }
                
                // 写入单元格数据（支持多个sheet）
                for (ExcelWriteRequest.CellData cellData : request.getCells()) {
                    // 确定使用哪个sheet（优先使用cell级别的sheetName）
                    String targetSheetName = cellData.getSheetName() != null 
                            ? cellData.getSheetName() 
                            : request.getSheetName();
                    
                    if (targetSheetName == null || targetSheetName.trim().isEmpty()) {
                        throw new IllegalArgumentException(
                            "单元格 " + cellData.getCellAddress() + " 未指定sheet名称");
                    }
                    
                    // 获取或创建sheet
                    Sheet sheet = getOrCreateSheet(workbook, targetSheetName);
                    
                    // 解析单元格地址（如"B1"、"AZ27"）
                    CellReference cellRef = new CellReference(cellData.getCellAddress());
                    int rowIdx = cellRef.getRow();
                    int colIdx = cellRef.getCol();
                    
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) {
                        row = sheet.createRow(rowIdx);
                    }
                    
                    Cell cell = row.getCell(colIdx);
                    if (cell == null) {
                        cell = row.createCell(colIdx);
                    }
                    
                    setCellValue(cell, cellData);
                }
                
                // 备份原文件（如果启用版本控制）
                if (excelFile.exists() && excelConfig.getStorage().getVersionControl()) {
                    backupFile(excelFile);
                }
                
                // 确保父目录存在
                File parentDir = excelFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    log.info("父目录不存在，正在创建: {}", parentDir.getAbsolutePath());
                    if (!parentDir.mkdirs()) {
                        throw new IOException("无法创建父目录: " + parentDir.getAbsolutePath());
                    }
                }
                
                // 写入文件
                try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                    workbook.write(fos);
                }
                
                // 统计涉及的sheet数量
                long sheetCount = request.getCells().stream()
                        .map(cell -> cell.getSheetName() != null ? cell.getSheetName() : request.getSheetName())
                        .distinct()
                        .count();
                
                log.info("成功写入Excel: {}, 涉及Sheet数: {}, 单元格数: {}", 
                        fileName, sheetCount, request.getCells().size());
                
            } finally {
                if (workbook != null) {
                    workbook.close();
                }
            }
            
        } catch (IOException e) {
            log.error("写入Excel失败: {}", fileName, e);
            throw new ExcelOperationException("写入Excel失败: " + e.getMessage(), e);
        } finally {
            if (lockAcquired) {
                lockManager.releaseWriteLock(fileName);
            }
        }
    }
    
    /**
     * 读取Excel数据
     */
    public ExcelResponse readExcel(ExcelReadRequest request) {
        String fileName = request.getFileName();
        boolean lockAcquired = false;
        
        try {
            // 获取读锁
            lockAcquired = lockManager.tryAcquireReadLock(
                    fileName, 
                    excelConfig.getLock().getWaitTime()
            );
            
            if (!lockAcquired) {
                throw new ExcelOperationException("无法获取文件读锁，请稍后重试: " + fileName);
            }
            
            File excelFile = getExcelFile(fileName);
            if (!excelFile.exists()) {
                throw new ExcelOperationException("Excel文件不存在: " + fileName);
            }
            
            try (FileInputStream fis = new FileInputStream(excelFile);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                // 创建公式计算器
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                
                List<ExcelResponse.CellResult> results = new ArrayList<>();
                
                // 读取单元格数据（支持多个sheet）
                for (ExcelReadRequest.CellPosition position : request.getCells()) {
                    // 确定使用哪个sheet（优先使用cell级别的sheetName）
                    String targetSheetName = position.getSheetName() != null 
                            ? position.getSheetName() 
                            : request.getSheetName();
                    
                    if (targetSheetName == null || targetSheetName.trim().isEmpty()) {
                        throw new IllegalArgumentException(
                            "单元格 " + position.getCellAddress() + " 未指定sheet名称");
                    }
                    
                    // 获取sheet
                    Sheet sheet = workbook.getSheet(targetSheetName);
                    if (sheet == null) {
                        throw new ExcelOperationException("Sheet不存在: " + targetSheetName);
                    }
                    
                    // 解析单元格地址（如"B1"、"AZ27"）
                    CellReference cellRef = new CellReference(position.getCellAddress());
                    int rowIdx = cellRef.getRow();
                    int colIdx = cellRef.getCol();
                    
                    Row row = sheet.getRow(rowIdx);
                    Cell cell = (row != null) ? row.getCell(colIdx) : null;
                    
                    ExcelResponse.CellResult result = extractCellData(
                            cell, 
                            position.getCellAddress(),
                            evaluator,
                            request.getReadFormula()
                    );
                    
                    // 在结果中添加sheet信息，方便识别
                    result.setSheetName(targetSheetName);
                    
                    results.add(result);
                }
                
                // 统计涉及的sheet数量
                long sheetCount = request.getCells().stream()
                        .map(cell -> cell.getSheetName() != null ? cell.getSheetName() : request.getSheetName())
                        .distinct()
                        .count();
                
                log.info("成功读取Excel: {}, 涉及Sheet数: {}, 单元格数: {}", 
                        fileName, sheetCount, request.getCells().size());
                
                return ExcelResponse.builder()
                        .success(true)
                        .message("读取成功")
                        .data(results)
                        .build();
            }
            
        } catch (IOException e) {
            log.error("读取Excel失败: {}", fileName, e);
            throw new ExcelOperationException("读取Excel失败: " + e.getMessage(), e);
        } finally {
            if (lockAcquired) {
                lockManager.releaseReadLock(fileName);
            }
        }
    }
    
    /**
     * 同时执行写入和读取操作
     */
    public ExcelResponse writeAndRead(ExcelWriteRequest writeRequest, ExcelReadRequest readRequest) {
        // 先写入
        if (writeRequest != null) {
            writeExcel(writeRequest);
        }
        
        // 再读取
        if (readRequest != null) {
            return readExcel(readRequest);
        }
        
        return ExcelResponse.builder()
                .success(true)
                .message("操作成功")
                .build();
    }
    
    /**
     * 设置单元格值
     */
    private void setCellValue(Cell cell, ExcelWriteRequest.CellData cellData) {
        if (cellData.getValue() == null) {
            cell.setBlank();
            return;
        }
        
        String valueType = cellData.getValueType().toUpperCase();
        
        switch (valueType) {
            case "NUMBER":
                if (cellData.getValue() instanceof Number) {
                    cell.setCellValue(((Number) cellData.getValue()).doubleValue());
                } else {
                    cell.setCellValue(Double.parseDouble(cellData.getValue().toString()));
                }
                break;
                
            case "BOOLEAN":
                cell.setCellValue(Boolean.parseBoolean(cellData.getValue().toString()));
                break;
                
            case "FORMULA":
                // 设置公式（不带等号）
                String formula = cellData.getValue().toString();
                if (formula.startsWith("=")) {
                    formula = formula.substring(1);
                }
                cell.setCellFormula(formula);
                break;
                
            case "STRING":
            default:
                cell.setCellValue(cellData.getValue().toString());
                break;
        }
    }
    
    /**
     * 提取单元格数据
     */
    private ExcelResponse.CellResult extractCellData(
            Cell cell,
            String cellAddress,
            FormulaEvaluator evaluator,
            boolean readFormula) {
        
        ExcelResponse.CellResult.CellResultBuilder builder = ExcelResponse.CellResult.builder()
                .cellAddress(cellAddress);
        
        if (cell == null) {
            return builder
                    .value(null)
                    .valueType("BLANK")
                    .build();
        }
        
        CellType cellType = cell.getCellType();
        
        // 如果是公式单元格
        if (cellType == CellType.FORMULA) {
            builder.formula(cell.getCellFormula());
            
            if (readFormula) {
                // 只返回公式
                builder.value("=" + cell.getCellFormula())
                       .valueType("FORMULA");
            } else {
                // 计算公式结果
                try {
                    CellValue cellValue = evaluator.evaluate(cell);
                    extractCellValue(builder, cellValue);
                } catch (Exception e) {
                    log.warn("公式计算失败: {}, 公式: {}", e.getMessage(), cell.getCellFormula());
                    builder.value("#ERROR: " + e.getMessage())
                           .valueType("ERROR");
                }
            }
        } else {
            // 非公式单元格
            extractDirectCellValue(builder, cell, cellType);
        }
        
        return builder.build();
    }
    
    /**
     * 提取计算后的单元格值
     */
    private void extractCellValue(ExcelResponse.CellResult.CellResultBuilder builder, CellValue cellValue) {
        switch (cellValue.getCellType()) {
            case NUMERIC:
                builder.value(cellValue.getNumberValue())
                       .valueType("NUMERIC");
                break;
            case STRING:
                builder.value(cellValue.getStringValue())
                       .valueType("STRING");
                break;
            case BOOLEAN:
                builder.value(cellValue.getBooleanValue())
                       .valueType("BOOLEAN");
                break;
            case ERROR:
                builder.value("#ERROR")
                       .valueType("ERROR");
                break;
            default:
                builder.value(null)
                       .valueType("BLANK");
        }
    }
    
    /**
     * 提取直接的单元格值
     */
    private void extractDirectCellValue(
            ExcelResponse.CellResult.CellResultBuilder builder, 
            Cell cell, 
            CellType cellType) {
        
        switch (cellType) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    builder.value(cell.getDateCellValue().toString())
                           .valueType("DATE");
                } else {
                    builder.value(cell.getNumericCellValue())
                           .valueType("NUMERIC");
                }
                break;
            case STRING:
                builder.value(cell.getStringCellValue())
                       .valueType("STRING");
                break;
            case BOOLEAN:
                builder.value(cell.getBooleanCellValue())
                       .valueType("BOOLEAN");
                break;
            case BLANK:
                builder.value(null)
                       .valueType("BLANK");
                break;
            default:
                builder.value(cell.toString())
                       .valueType("UNKNOWN");
        }
    }
    
    /**
     * 获取或创建Sheet
     */
    private Sheet getOrCreateSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            log.info("创建新Sheet: {}", sheetName);
        }
        return sheet;
    }
    
    /**
     * 获取Excel文件
     */
    private File getExcelFile(String fileName) {
        File storageDir = excelConfig.getStorage().getStorageDir();
        return new File(storageDir, fileName);
    }
    
    /**
     * 备份文件
     */
    private void backupFile(File originalFile) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = originalFile.getName().replace(".xlsx", "_" + timestamp + ".xlsx");
        File backupFile = new File(originalFile.getParent(), "backup_" + backupFileName);
        
        Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.debug("备份文件: {} -> {}", originalFile.getName(), backupFile.getName());
        
        // 清理旧版本
        cleanOldBackups(originalFile);
    }
    
    /**
     * 清理旧备份
     */
    private void cleanOldBackups(File originalFile) {
        File[] backupFiles = originalFile.getParentFile().listFiles((dir, name) -> 
                name.startsWith("backup_") && name.contains(originalFile.getName().replace(".xlsx", ""))
        );
        
        if (backupFiles != null && backupFiles.length > excelConfig.getStorage().getMaxVersions()) {
            // 按修改时间排序
            java.util.Arrays.sort(backupFiles, (f1, f2) -> 
                    Long.compare(f1.lastModified(), f2.lastModified())
            );
            
            // 删除最旧的文件
            int deleteCount = backupFiles.length - excelConfig.getStorage().getMaxVersions();
            for (int i = 0; i < deleteCount; i++) {
                if (backupFiles[i].delete()) {
                    log.debug("删除旧备份: {}", backupFiles[i].getName());
                }
            }
        }
    }
}


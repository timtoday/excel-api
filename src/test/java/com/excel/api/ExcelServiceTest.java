package com.excel.api;

import com.excel.api.model.ExcelReadRequest;
import com.excel.api.model.ExcelResponse;
import com.excel.api.model.ExcelWriteRequest;
import com.excel.api.service.ExcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Excel服务测试
 */
@SpringBootTest
public class ExcelServiceTest {
    
    @Autowired
    private ExcelService excelService;
    
    @Test
    public void testWriteAndRead() {
        // 准备写入数据
        ExcelWriteRequest writeRequest = new ExcelWriteRequest();
        writeRequest.setFileName("test.xlsx");
        writeRequest.setSheetName("TestSheet");
        
        ExcelWriteRequest.CellData cell1 = new ExcelWriteRequest.CellData();
        cell1.setCellAddress("A1");
        cell1.setValue("测试");
        cell1.setValueType("STRING");
        
        ExcelWriteRequest.CellData cell2 = new ExcelWriteRequest.CellData();
        cell2.setCellAddress("A2");
        cell2.setValue(100);
        cell2.setValueType("NUMBER");
        
        writeRequest.setCells(Arrays.asList(cell1, cell2));
        
        // 写入
        excelService.writeExcel(writeRequest);
        
        // 准备读取数据
        ExcelReadRequest readRequest = new ExcelReadRequest();
        readRequest.setFileName("test.xlsx");
        readRequest.setSheetName("TestSheet");
        
        ExcelReadRequest.CellPosition pos1 = new ExcelReadRequest.CellPosition();
        pos1.setCellAddress("A1");
        
        ExcelReadRequest.CellPosition pos2 = new ExcelReadRequest.CellPosition();
        pos2.setCellAddress("A2");
        
        readRequest.setCells(Arrays.asList(pos1, pos2));
        readRequest.setReadFormula(false);
        
        // 读取
        ExcelResponse response = excelService.readExcel(readRequest);
        
        // 验证
        assertTrue(response.getSuccess());
        assertEquals(2, response.getData().size());
        assertEquals("测试", response.getData().get(0).getValue());
        assertEquals(100.0, response.getData().get(1).getValue());
    }
    
    @Test
    public void testFormulaCalculation() {
        // 准备写入数据和公式
        ExcelWriteRequest writeRequest = new ExcelWriteRequest();
        writeRequest.setFileName("formula_test.xlsx");
        writeRequest.setSheetName("Sheet1");
        
        ExcelWriteRequest.CellData cell1 = new ExcelWriteRequest.CellData();
        cell1.setCellAddress("A1");
        cell1.setValue(10);
        cell1.setValueType("NUMBER");
        
        ExcelWriteRequest.CellData cell2 = new ExcelWriteRequest.CellData();
        cell2.setCellAddress("B1");
        cell2.setValue(20);
        cell2.setValueType("NUMBER");
        
        ExcelWriteRequest.CellData formulaCell = new ExcelWriteRequest.CellData();
        formulaCell.setCellAddress("C1");
        formulaCell.setValue("A1+B1");
        formulaCell.setValueType("FORMULA");
        
        writeRequest.setCells(Arrays.asList(cell1, cell2, formulaCell));
        
        // 写入
        excelService.writeExcel(writeRequest);
        
        // 读取公式结果
        ExcelReadRequest readRequest = new ExcelReadRequest();
        readRequest.setFileName("formula_test.xlsx");
        readRequest.setSheetName("Sheet1");
        
        ExcelReadRequest.CellPosition pos = new ExcelReadRequest.CellPosition();
        pos.setCellAddress("C1");
        
        readRequest.setCells(Arrays.asList(pos));
        readRequest.setReadFormula(false);
        
        // 读取
        ExcelResponse response = excelService.readExcel(readRequest);
        
        // 验证
        assertTrue(response.getSuccess());
        assertEquals(1, response.getData().size());
        assertEquals(30.0, response.getData().get(0).getValue());
        assertEquals("A1+B1", response.getData().get(0).getFormula());
    }
}


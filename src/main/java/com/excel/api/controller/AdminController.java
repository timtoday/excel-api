package com.excel.api.controller;

import com.excel.api.config.AdminConfig;
import com.excel.api.config.ExcelConfig;
import com.excel.api.model.ApiToken;
import com.excel.api.model.RequestLog;
import com.excel.api.service.RequestLogService;
import com.excel.api.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private ExcelConfig excelConfig;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private RequestLogService requestLogService;
    
    @Autowired
    private AdminConfig adminConfig;
    
    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * 控制台首页
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("currentPage", "dashboard");
        
        // 统计信息
        List<ApiToken> tokens = tokenService.getAllTokens();
        List<RequestLog> recentLogs = requestLogService.getRecentLogs(10);
        
        model.addAttribute("tokenCount", tokens.size());
        model.addAttribute("activeTokenCount", tokens.stream().filter(ApiToken::isActive).count());
        model.addAttribute("totalRequests", requestLogService.getAllLogs().size());
        model.addAttribute("recentLogs", recentLogs);
        
        return "dashboard";
    }
    
    /**
     * Excel文件管理页面
     */
    @GetMapping("/files")
    public String files(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("currentPage", "files");
        
        File storageDir = excelConfig.getStorage().getStorageDir();
        log.debug("文件列表 - 存储目录: {}", storageDir.getAbsolutePath());
        log.debug("文件列表 - 目录存在: {}", storageDir.exists());
        log.debug("文件列表 - 是否目录: {}", storageDir.isDirectory());
        
        List<FileInfo> files = new java.util.ArrayList<>();
        
        // 检查目录是否存在且为目录
        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] fileArray = storageDir.listFiles();
            if (fileArray != null) {
                log.debug("文件列表 - 目录中文件数: {}", fileArray.length);
                files = Arrays.stream(fileArray)
                        .filter(f -> {
                            boolean isFile = f.isFile();
                            boolean isExcel = f.getName().endsWith(".xlsx") || f.getName().endsWith(".xls");
                            boolean isNotGitKeep = !f.getName().equals(".gitkeep");
                            log.debug("文件: {} - isFile: {}, isExcel: {}, notGitKeep: {}", 
                                    f.getName(), isFile, isExcel, isNotGitKeep);
                            return isFile && isExcel && isNotGitKeep;
                        })
                        .map(f -> new FileInfo(
                                f.getName(),
                                f.length() / 1024.0,  // KB
                                new java.util.Date(f.lastModified())
                        ))
                        .sorted((a, b) -> b.getLastModified().compareTo(a.getLastModified()))
                        .collect(Collectors.toList());
            } else {
                log.warn("文件列表 - listFiles()返回null");
            }
        } else {
            log.warn("文件列表 - 存储目录不存在或不是目录");
        }
        
        log.info("文件列表 - 找到 {} 个Excel文件", files.size());
        
        model.addAttribute("files", files);
        model.addAttribute("totalFiles", files.size());
        model.addAttribute("totalSize", files.stream().mapToDouble(FileInfo::getSizeKb).sum());
        
        return "files";
    }
    
    /**
     * 上传Excel文件
     */
    @PostMapping("/files/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                            RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请选择文件");
                return "redirect:/admin/files";
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                redirectAttributes.addFlashAttribute("error", "只支持Excel文件（.xlsx或.xls）");
                return "redirect:/admin/files";
            }
            
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
            File targetFile = new File(storageDir.getAbsolutePath(), filename);
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
            
            // 验证文件是否成功保存
            if (targetFile.exists()) {
                log.info("✓ 文件上传成功: {} -> {}", filename, targetFile.getAbsolutePath());
                log.info("✓ 文件大小: {} bytes", targetFile.length());
                log.info("✓ 文件可读: {}", targetFile.canRead());
                redirectAttributes.addFlashAttribute("success", "文件上传成功: " + filename);
            } else {
                log.error("✗ 文件保存失败，文件不存在: {}", targetFile.getAbsolutePath());
                redirectAttributes.addFlashAttribute("error", "文件保存失败，请检查日志");
            }
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            redirectAttributes.addFlashAttribute("error", "上传失败: " + e.getMessage());
        }
        
        return "redirect:/admin/files";
    }
    
    /**
     * 删除Excel文件
     */
    @PostMapping("/files/delete")
    public String deleteFile(@RequestParam("filename") String filename,
                            RedirectAttributes redirectAttributes) {
        try {
            File file = new File(excelConfig.getStorage().getStorageDir(), filename);
            if (file.exists() && file.delete()) {
                redirectAttributes.addFlashAttribute("success", "文件删除成功: " + filename);
                log.info("删除文件: {}", filename);
            } else {
                redirectAttributes.addFlashAttribute("error", "文件删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败", e);
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        
        return "redirect:/admin/files";
    }
    
    /**
     * Token管理页面
     */
    @GetMapping("/tokens")
    public String tokens(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("currentPage", "tokens");
        
        List<ApiToken> tokens = tokenService.getAllTokens();
        model.addAttribute("tokens", tokens);
        model.addAttribute("defaultExpiryDays", adminConfig.getTokens().getDefaultExpiryDays());
        
        return "tokens";
    }
    
    /**
     * 创建Token
     */
    @PostMapping("/tokens/create")
    public String createToken(@RequestParam("name") String name,
                             @RequestParam("description") String description,
                             @RequestParam(value = "expiryDays", required = false) Integer expiryDays,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            ApiToken token = tokenService.createToken(name, description, auth.getName(), expiryDays);
            redirectAttributes.addFlashAttribute("success", "Token创建成功");
            redirectAttributes.addFlashAttribute("newToken", token.getToken());
            redirectAttributes.addFlashAttribute("tokenName", token.getName());
            
            log.info("创建Token: {} by {}", name, auth.getName());
            
        } catch (Exception e) {
            log.error("创建Token失败", e);
            redirectAttributes.addFlashAttribute("error", "创建失败: " + e.getMessage());
        }
        
        return "redirect:/admin/tokens";
    }
    
    /**
     * 禁用/启用Token
     */
    @PostMapping("/tokens/toggle")
    public String toggleToken(@RequestParam("token") String token,
                             RedirectAttributes redirectAttributes) {
        try {
            ApiToken apiToken = tokenService.getToken(token);
            if (apiToken != null) {
                if (apiToken.isActive()) {
                    tokenService.disableToken(token);
                    redirectAttributes.addFlashAttribute("success", "Token已禁用");
                } else {
                    tokenService.enableToken(token);
                    redirectAttributes.addFlashAttribute("success", "Token已启用");
                }
            }
        } catch (Exception e) {
            log.error("切换Token状态失败", e);
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        
        return "redirect:/admin/tokens";
    }
    
    /**
     * 删除Token
     */
    @PostMapping("/tokens/delete")
    public String deleteToken(@RequestParam("token") String token,
                             RedirectAttributes redirectAttributes) {
        try {
            tokenService.deleteToken(token);
            redirectAttributes.addFlashAttribute("success", "Token已删除");
        } catch (Exception e) {
            log.error("删除Token失败", e);
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        
        return "redirect:/admin/tokens";
    }
    
    /**
     * 请求日志页面
     */
    @GetMapping("/logs")
    public String logs(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("currentPage", "logs");
        
        List<RequestLog> logs = requestLogService.getRecentLogs(100);
        model.addAttribute("logs", logs);
        model.addAttribute("stats", requestLogService.getStatistics());
        
        return "logs";
    }
    
    /**
     * 清空日志
     */
    @PostMapping("/logs/clear")
    public String clearLogs(RedirectAttributes redirectAttributes) {
        requestLogService.clearLogs();
        redirectAttributes.addFlashAttribute("success", "日志已清空");
        return "redirect:/admin/logs";
    }
    
    /**
     * API测试页面
     */
    @GetMapping("/test")
    public String test(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("currentPage", "test");
        
        List<ApiToken> tokens = tokenService.getAllTokens().stream()
                .filter(ApiToken::isActive)
                .collect(Collectors.toList());
        model.addAttribute("tokens", tokens);
        
        File storageDir = excelConfig.getStorage().getStorageDir();
        List<String> files = Arrays.stream(storageDir.listFiles())
                .filter(f -> f.isFile() && (f.getName().endsWith(".xlsx") || f.getName().endsWith(".xls")))
                .map(File::getName)
                .collect(Collectors.toList());
        model.addAttribute("files", files);
        
        return "test";
    }
    
    /**
     * 文件信息内部类
     */
    public static class FileInfo {
        private String name;
        private double sizeKb;
        private java.util.Date lastModified;
        
        public FileInfo(String name, double sizeKb, java.util.Date lastModified) {
            this.name = name;
            this.sizeKb = sizeKb;
            this.lastModified = lastModified;
        }
        
        public String getName() { return name; }
        public double getSizeKb() { return sizeKb; }
        public java.util.Date getLastModified() { return lastModified; }
    }
}


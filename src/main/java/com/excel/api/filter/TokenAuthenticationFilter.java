package com.excel.api.filter;

import com.excel.api.model.RequestLog;
import com.excel.api.service.RequestLogService;
import com.excel.api.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Token认证过滤器
 */
@Slf4j
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private RequestLogService requestLogService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // 只对API接口进行Token验证
        if (!path.startsWith("/api/excel")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 包装请求和响应以便读取内容
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取Token
            String token = extractToken(request);
            
            if (token == null || token.isEmpty()) {
                sendErrorResponse(wrappedResponse, 401, "缺少Token");
                logRequest(wrappedRequest, wrappedResponse, null, startTime, "缺少Token");
                return;
            }
            
            // 验证Token
            if (!tokenService.validateToken(token)) {
                sendErrorResponse(wrappedResponse, 403, "无效或已过期的Token");
                logRequest(wrappedRequest, wrappedResponse, token, startTime, "Token验证失败");
                return;
            }
            
            // 继续处理请求
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
            // 记录成功的请求
            logRequest(wrappedRequest, wrappedResponse, token, startTime, null);
            
        } catch (Exception e) {
            log.error("Token验证过程出错", e);
            sendErrorResponse(wrappedResponse, 500, "服务器内部错误");
            logRequest(wrappedRequest, wrappedResponse, null, startTime, e.getMessage());
        } finally {
            wrappedResponse.copyBodyToResponse();
        }
    }
    
    /**
     * 提取Token
     */
    private String extractToken(HttpServletRequest request) {
        // 从Header中获取
        String token = request.getHeader("X-API-Token");
        if (token != null && !token.isEmpty()) {
            return token;
        }
        
        // 从Authorization Header获取
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        
        // 从查询参数获取
        return request.getParameter("token");
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        
        String json = String.format("{\"success\":false,\"message\":\"%s\"}", message);
        response.getWriter().write(json);
    }
    
    /**
     * 记录请求日志
     */
    private void logRequest(ContentCachingRequestWrapper request,
                           ContentCachingResponseWrapper response,
                           String token,
                           long startTime,
                           String errorMessage) {
        
        long duration = System.currentTimeMillis() - startTime;
        
        String requestBody = getRequestBody(request);
        String responseBody = getResponseBody(response);
        
        RequestLog requestLog = RequestLog.builder()
                .method(request.getMethod())
                .path(request.getRequestURI())
                .token(token)
                .tokenName(token != null ? getTokenName(token) : null)
                .statusCode(response.getStatus())
                .duration(duration)
                .requestBody(requestBody)
                .responseBody(responseBody)
                .errorMessage(errorMessage)
                .clientIp(getClientIp(request))
                .build();
        
        requestLogService.logRequest(requestLog);
    }
    
    /**
     * 获取请求体
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                // 限制长度
                return body.length() > 1000 ? body.substring(0, 1000) + "..." : body;
            }
        } catch (Exception e) {
            log.warn("无法读取请求体", e);
        }
        return null;
    }
    
    /**
     * 获取响应体
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                // 限制长度
                return body.length() > 1000 ? body.substring(0, 1000) + "..." : body;
            }
        } catch (Exception e) {
            log.warn("无法读取响应体", e);
        }
        return null;
    }
    
    /**
     * 获取Token名称
     */
    private String getTokenName(String token) {
        try {
            var apiToken = tokenService.getToken(token);
            return apiToken != null ? apiToken.getName() : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}


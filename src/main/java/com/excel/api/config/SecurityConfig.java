package com.excel.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private AdminConfig adminConfig;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 公开的API接口（需要token验证）
                .requestMatchers("/api/**").permitAll()
                // Swagger文档
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                // 静态资源
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // 登录页面
                .requestMatchers("/admin/login").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf
                // API接口禁用CSRF
                .ignoringRequestMatchers("/api/**")
            );
        
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        List<UserDetails> users = new ArrayList<>();
        
        for (AdminConfig.AdminUser adminUser : adminConfig.getUsers()) {
            users.add(User.builder()
                    .username(adminUser.getUsername())
                    .password(passwordEncoder().encode(adminUser.getPassword()))
                    .roles(adminUser.getRole())
                    .build());
        }
        
        return new InMemoryUserDetailsManager(users);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


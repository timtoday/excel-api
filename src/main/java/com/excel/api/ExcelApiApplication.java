package com.excel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Excel API服务启动类
 * 排除Redis和Redisson自动配置，使用本地锁
 * 使用excludeName通过字符串排除不在classpath中的类
 */
@SpringBootApplication(excludeName = {
    "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
    "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
    "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
    "org.springframework.boot.actuate.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration",
    "org.springframework.boot.actuate.autoconfigure.data.redis.RedisReactiveHealthContributorAutoConfiguration",
    "org.redisson.spring.starter.RedissonAutoConfiguration",
    "org.redisson.spring.starter.RedissonAutoConfigurationV2"
})
public class ExcelApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelApiApplication.class, args);
    }
}


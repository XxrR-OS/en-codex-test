package com.english.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * 英语学习平台 - 主启动类
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.english.platform.mapper")
public class EnglishLearningPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnglishLearningPlatformApplication.class, args);
        System.out.println("==============================================");
        System.out.println("  英语学习平台启动成功！");
        System.out.println("  http://localhost:8080");
        System.out.println("==============================================");
    }
}

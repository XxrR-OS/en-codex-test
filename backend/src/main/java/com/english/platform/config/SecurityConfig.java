package com.english.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全配置 —— 仅提供 BCrypt 密码编码器 Bean
 * 不引入 Spring Security 完整的自动配置（通过排除 SecurityAutoConfiguration 实现）
 */
@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

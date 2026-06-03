package com.english.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 主启动测试 - 验证 Spring 上下文是否能正常加载
 * 运行前请确保 MySQL 已启动，且 application.yml 中数据库配置正确
 */
@SpringBootTest
class EnglishLearningPlatformApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("✅ Spring 上下文加载成功！");
    }
}

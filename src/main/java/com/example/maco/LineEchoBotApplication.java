package com.example.maco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableJpaAuditing
public class LineEchoBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(LineEchoBotApplication.class, args);
    }

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 基本執行緒數
        executor.setMaxPoolSize(60); // 最大執行緒數
        executor.setQueueCapacity(100); // 等待佇列長度
        executor.setThreadNamePrefix("LineBot-Async-");
        executor.initialize();
        return executor;
    }
}
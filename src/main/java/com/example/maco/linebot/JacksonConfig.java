package com.example.maco.linebot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class JacksonConfig {
    @Bean
    @Primary // 將這個 Bean 設為主要的 ObjectMapper
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 所有您需要的設定，都在這裡一次性完成
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 如果您未來需要處理 Java 8 的時間，可以加上這個官方模組，非常推薦
        // mapper.registerModule(new JavaTimeModule());

        return mapper;
    }
}

package com.example.maco.linebot.util;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

public class NlpClient {
    private static final Logger log = LoggerFactory.getLogger(NlpClient.class);
    private static final String BASE_URL = System.getenv().getOrDefault("NLP_BASE_URL", "http://localhost:8000");

    public static <T, R> R callNlpApi(String path, T request, Class<R> responseType) {
        try {
            WebClient webClient = WebClient.create(BASE_URL);
            R response = webClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(responseType)
                    // 最小 retry：最多重試 3 次，短暫 backoff
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(300)))
                    .block();
            log.info("NLP {} response: {}", path, response);
            return response;
        } catch (Exception e) {
            log.error("NLP {} 呼叫失敗", path, e);
            return null;
        }
    }

    public static <T, R> Mono<R> callNlpApiAsync(String path, T request, Class<R> responseType) {
        WebClient webClient = WebClient.create(BASE_URL);
        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(responseType)
                .doOnNext(res -> log.info("NLP {} response: {}", path, res))
                .doOnError(e -> log.error("NLP {} 呼叫失敗", path, e))
                // 最小 retry：最多重試 3 次，短暫 backoff
                .retryWhen(Retry.backoff(3, Duration.ofMillis(300)));
    }
}

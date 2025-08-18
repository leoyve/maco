package com.example.maco.linebot;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.maco.linebot.model.LineMessageDto;
import com.example.maco.linebot.model.RouterRequest;
import com.example.maco.linebot.model.RouterResponse;
import com.example.maco.linebot.util.NlpClient;
import com.example.maco.adapters.db.jpa.LineUserMessageMapper;
import com.example.maco.adapters.db.jpa.LineUserMessageRepository;
import com.example.maco.adapters.db.jpa.LineUserMessage;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.messaging.model.PushMessageRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.maco.linebot.model.ProcessRequest;
import com.example.maco.linebot.model.ProcessResponse;

@Service
public class LineService {

    private static final Logger log = LoggerFactory.getLogger(LineService.class);

    private final MessagingApiClient messagingApiClient;

    private final LineUserMessageRepository lineUserMessageRepository;

    public LineService(MessagingApiClient messagingApiClient,
            LineUserMessageRepository lineUserMessageRepository) {
        this.messagingApiClient = messagingApiClient;
        this.lineUserMessageRepository = lineUserMessageRepository;
    }

    public String echoMessage(String message) {
        return message;
    }

    @Async
    @Transactional
    public CompletableFuture<Void> handleTextMessage(LineMessageDto lineMessageDto) {
        // 先將訊息存進DB
        LineUserMessage entity = LineUserMessageMapper.toEntity(lineMessageDto);
        lineUserMessageRepository.save(entity);
        lineUserMessageRepository.flush();

        // 呼叫 NLPService 判斷 domain (非同步)
        CompletableFuture<RouterResponse> routerFuture = CompletableFuture.supplyAsync(() -> NlpClient
                .callNlpApi("/router", new RouterRequest(lineMessageDto.getMessage()), RouterResponse.class));

        return routerFuture.thenAccept(routerRes -> {
            String domain = routerRes != null ? routerRes.getDomain() : "unknown";
            log.info("NLPService 判斷 domain: {}", domain);
            switch (domain) {
                case "todo":
                    handleTodoAsync(lineMessageDto, domain);
                    break;
                case "health":
                    handleHealthAsync(lineMessageDto, domain);
                    break;
                default:
                    log.info("未知 domain，僅儲存訊息");
            }
        });
    }

    // 各 domain 對應的 Service 處理方法 (非同步)
    @Async
    public CompletableFuture<Void> handleTodoAsync(LineMessageDto dto, String domain) {
        log.info("處理代辦事項 domain: {}", domain);
        CompletableFuture<ProcessResponse> processFuture = CompletableFuture.supplyAsync(() -> NlpClient
                .callNlpApi("/process", new ProcessRequest(domain, dto.getMessage()), ProcessResponse.class));
        return processFuture.thenAccept(processRes -> {
            log.info("NLP /process response: {}", processRes != null ? processRes.getResult() : "null");
        });
    }

    @Async
    public CompletableFuture<Void> handleHealthAsync(LineMessageDto dto, String domain) {
        log.info("處理健康 domain: {}", domain);
        // 可依需求串接 /process 或其他服務
        return CompletableFuture.completedFuture(null);
    }

    public void sendReply(String replyToken, String message) {

        // 建立一個要回覆的文字訊息
        TextMessage replyText = new TextMessage("You Said: " + message); // Echo Bot: 回覆收到的同樣訊息

        // 使用 replyToken 來回覆訊息
        ReplyMessageRequest replyMessageRequest = new ReplyMessageRequest(
                replyToken,
                List.of(replyText), // 使用 List.of() 快速建立一個 List
                false);

        // 執行發送
        try {
            messagingApiClient.replyMessage(replyMessageRequest).get();
        } catch (Exception e) {
            log.error("Reply failed", e);
        }
    }

    public void pushMessage(String userId, String message) {
        TextMessage pushText = new TextMessage(message);
        PushMessageRequest pushMessageRequest = new PushMessageRequest(
                userId,
                List.of(pushText),
                false, // notificationDisabled
                null // customAggregationUnits
        );
        try {
            messagingApiClient.pushMessage(UUID.randomUUID(), pushMessageRequest).get();
        } catch (Exception e) {
            log.error("Push failed", e);
        }
    }
}
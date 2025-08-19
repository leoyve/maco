package com.example.maco.linebot;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.maco.linebot.model.RouterRequest;
import com.example.maco.linebot.model.RouterResponse;
import com.example.maco.linebot.util.NlpClient;
import com.example.maco.service.todo.TodoService;
import com.example.maco.service.user.LineMessageService;
import com.example.maco.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.maco.domain.dto.LineMessageDto;
import com.example.maco.domain.dto.TodoResultDto;
import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.domain.model.user.LineMessage;
import com.example.maco.infra.jpa.mapper.LineMessageMapper;
import com.example.maco.infra.jpa.mapper.TodoMapper;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.messaging.model.PushMessageRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.maco.linebot.model.ProcessRequest;
import com.example.maco.linebot.model.ProcessResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LineService {

    private static final Logger log = LoggerFactory.getLogger(LineService.class);

    private final MessagingApiClient messagingApiClient;
    private final LineMessageService lineMessageService;
    private final UserService userService;

    private final TodoService todoService;

    public String echoMessage(String message) {
        return message;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> handleTextMessage(LineMessageDto lineMessageDto) {
        lineMessageService.saveMessage(LineMessageMapper.toDomain(lineMessageDto));
        LineMessage lineMessage = LineMessageMapper.toDomain(lineMessageDto);
        userService.upsertUser(lineMessage.getUserId());

        return NlpClient
                .callNlpApiAsync("/router", new RouterRequest(lineMessage.getMessage()), RouterResponse.class)
                .timeout(Duration.ofSeconds(20)) // 5. NLP API 超時設定
                .toFuture()
                .thenAccept(routerRes -> {
                    String domain = routerRes != null ? routerRes.getDomain() : "unknown";
                    log.info("NLPService 判斷 domain: {}, userId: {}, messageId: {}", domain, lineMessage.getUserId(),
                            lineMessage.getMessageId()); // 7. 日誌優化
                    switch (domain) {
                        case "todo":
                            handleTodoAsync(lineMessage, domain)
                                    .exceptionally(e -> {
                                        log.error("handleTodoAsync error", e);
                                        return null;
                                    }); // 1. 異常處理
                            break;
                        case "health":
                            handleHealthAsync(lineMessage, domain)
                                    .exceptionally(e -> {
                                        log.error("handleHealthAsync error", e);
                                        return null;
                                    }); // 1. 異常處理
                            break;
                        default:
                            log.info("未知 domain，僅儲存訊息, userId: {}, messageId: {}", lineMessage.getUserId(),
                                    lineMessage.getMessageId()); // 7. 日誌優化
                            sendReply(lineMessage.getReplyToken(), "未知的請求，請嘗試：\n1. 新增代辦事項\n2. 新增體重\n");
                    }
                })
                .exceptionally(e -> {
                    log.error("NLP /router error, userId: {}, messageId: {}", lineMessage.getUserId(),
                            lineMessage.getMessageId(), e);
                    sendReply(lineMessage.getReplyToken(), "系統出現些許錯誤，請稍後再試");
                    return null;
                }); // 1. 異常處理
    }

    // 各 domain 對應的 Service 處理方法 (非同步)
    @Async("taskExecutor")
    public CompletableFuture<Void> handleTodoAsync(LineMessage model, String domain) {
        log.info("處理代辦事項 domain: {}, userId: {}, messageId: {}", domain, model.getUserId(), model.getMessageId());
        return NlpClient
                .callNlpApiAsync("/process", new ProcessRequest(domain, model.getMessage()), ProcessResponse.class)
                .timeout(Duration.ofSeconds(20))
                .toFuture()
                .thenAccept(processRes -> {
                    log.info("NLP /process response: {}, userId: {}, messageId: {}",
                            processRes != null ? processRes.getResult() : "null", model.getUserId(),
                            model.getMessageId());
                    // 6. 處理 NLP 回傳結果
                    if (processRes != null && processRes.getResult() != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        TodoResultDto todoResultDto = mapper.convertValue(processRes.getResult(), TodoResultDto.class);
                        TodoResult todoResult = TodoMapper.toDomain(todoResultDto);

                        if (todoResult == null) {
                            log.warn("TodoResultDto is null, userId: {}, messageId: {}", model.getUserId(),
                                    model.getMessageId());
                            sendReply(model.getReplyToken(), "[代辦事項] 無法處理您的請求，請稍後再試");
                            return;
                        }
                        if (!todoResult.isClear()) {
                            sendReply(model.getReplyToken(), "[代辦事項] " + todoResult.getRecommendation());
                            return;
                        }

                        // TODO 增加ＤＢ層邏輯

                        // 依 intent 組合更友善訊息
                        String reply;
                        if ("addTodo".equals(todoResult.getIntent())) {
                            todoService.insertTodo(todoResult);
                            reply = "已新增代辦：「" + todoResult.getEntities().getTask() + "」";
                        } else if ("queryTodo".equals(todoResult.getIntent())) {
                            reply = "時間區間 [ 開始日："
                                    + todoResult.getEntities().getTime().getStartDate()
                                    + "，結束日：" + todoResult.getEntities().getTime().getEndDate() + " ]"
                                    + "，代辦事項：" + todoResult.getEntities().getTask();
                        } else {
                            reply = "[代辦事項] " + todoResult.getEntities().getTask();
                        }
                        sendReply(model.getReplyToken(), reply);
                    } else {
                        log.warn("ProcessResponse result is null, userId: {}, messageId: {}", model.getUserId(),
                                model.getMessageId());
                        sendReply(model.getReplyToken(), "[代辦事項] 無法處理您的請求，請稍後再試");
                    }
                }).exceptionally(e -> {
                    log.error("NLP /process error, userId: {}, messageId: {}", model.getUserId(), model.getMessageId(),
                            e);
                    sendReply(model.getReplyToken(), "[代辦事項] 系統出現些許錯誤，請稍後再試");
                    return null;
                });
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> handleHealthAsync(LineMessage model, String domain) {
        log.info("處理健康 domain: {}, userId: {}, messageId: {}", domain, model.getUserId(), model.getMessageId());
        return NlpClient
                .callNlpApiAsync("/process", new ProcessRequest(domain, model.getMessage()), ProcessResponse.class)
                .timeout(Duration.ofSeconds(20))
                .toFuture()
                .thenAccept(processRes -> {
                    // log.info("NLP /process response: {}, userId: {}, messageId: {}",
                    // processRes != null ? p
                    // .getMessageId());

                }).exceptionally(e -> {

                    sendReply(model.getReplyToken(), "[健康] 系統出現些許錯誤，請稍後再試");

                    return null;
                });
    }

    public void sendReply(String replyToken, String message) {
        TextMessage replyText = new TextMessage("You Said: " + message);
        ReplyMessageRequest replyMessageRequest = new ReplyMessageRequest(
                replyToken,
                List.of(replyText),
                false);
        try {
            messagingApiClient.replyMessage(replyMessageRequest).get();
            log.info("Reply success, replyToken: {}, message: {}", replyToken, message);
        } catch (Exception e) {
            log.error("Reply failed, replyToken: {}, message: {}", replyToken, message, e);
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

    public void mockPushMessage(String userId, String message) {
        log.info("Mock push message to userId: {}, message: {}", userId, message);
    }

}
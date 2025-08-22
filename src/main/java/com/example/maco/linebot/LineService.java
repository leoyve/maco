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
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import com.linecorp.bot.messaging.model.FlexContainer;
import com.linecorp.bot.messaging.model.FlexMessage;

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

    private final LineFlexMessageBuilder lineFlexMessageBuilder;

    public String echoMessage(String message) {
        return message;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> handleTextMessage(LineMessageDto lineMessageDto) {
        lineMessageService.saveMessage(LineMessageMapper.toDomain(lineMessageDto));
        LineMessage lineMessage = LineMessageMapper.toDomain(lineMessageDto);
        userService.upsertUser(lineMessage.getUserToken());

        return NlpClient
                .callNlpApiAsync("/router", new RouterRequest(lineMessage.getMessage()), RouterResponse.class)
                .timeout(Duration.ofSeconds(20)) // 5. NLP API 超時設定
                .toFuture()
                .thenAccept(routerRes -> {
                    String domain = routerRes != null ? routerRes.getDomain() : "unknown";
                    log.info("NLPService 判斷 domain: {}, userId: {}, messageId: {}", domain, lineMessage.getUserToken(),
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
                            log.info("未知 domain，僅儲存訊息, userId: {}, messageId: {}", lineMessage.getUserToken(),
                                    lineMessage.getMessageId()); // 7. 日誌優化
                            sendReply(lineMessage.getReplyToken(), "未知的請求，請嘗試：\n1. 新增代辦事項\n2. 新增體重\n");
                    }
                })
                .exceptionally(e -> {
                    log.error("NLP /router error, userId: {}, messageId: {}", lineMessage.getUserToken(),
                            lineMessage.getMessageId(), e);
                    sendReply(lineMessage.getReplyToken(), "系統出現些許錯誤，請稍後再試");
                    return null;
                }); // 1. 異常處理
    }

    // 各 domain 對應的 Service 處理方法 (非同步)
    @Async("taskExecutor")
    public CompletableFuture<Void> handleTodoAsync(LineMessage model, String domain) {
        log.info("處理代辦事項 domain: {}, userId: {}, messageId: {}", domain, model.getUserToken(), model.getMessageId());
        return NlpClient
                .callNlpApiAsync("/process", new ProcessRequest(domain, model.getMessage()), ProcessResponse.class)
                .timeout(Duration.ofSeconds(20))
                .toFuture()
                .thenAccept(processRes -> {
                    log.info("NLP /process response: {}, userId: {}, messageId: {}",
                            processRes != null ? processRes.getResult() : "null", model.getUserToken(),
                            model.getMessageId());
                    // 6. 處理 NLP 回傳結果
                    if (processRes != null && processRes.getResult() != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        TodoResultDto todoResultDto = mapper.convertValue(processRes.getResult(), TodoResultDto.class);
                        TodoResult todoResult = TodoMapper.toDomain(todoResultDto);

                        if (todoResult == null) {
                            log.warn("TodoResultDto is null, userId: {}, messageId: {}", model.getUserToken(),
                                    model.getMessageId());
                            sendReply(model.getReplyToken(), "[代辦事項] 無法處理您的請求，請稍後再試");
                            return;
                        }

                        if (!todoResult.isClear()) {
                            sendReply(model.getReplyToken(), "[代辦事項] " + todoResult.getRecommendation());
                            return;
                        }

                        // 依 intent 組合更友善訊息
                        String reply;
                        if ("addTodo".equals(todoResult.getIntent())) {
                            try {
                                todoService.insertTodo(model.getUserToken(), todoResult);
                                reply = todoResult.toUserMessageForAdd();
                                sendReply(model.getReplyToken(), reply);
                                log.info("新增代辦成功, userId: {}, messageId: {}", model.getUserToken(),
                                        model.getMessageId());
                            } catch (Exception ex) {
                                log.error("新增代辦失敗, userId: {}, messageId: {}", model.getUserToken(),
                                        model.getMessageId(),
                                        ex);
                                reply = "新增代辦失敗，請稍後再試";
                                sendReply(model.getReplyToken(), reply);
                            }
                        } else if ("queryTodo".equals(todoResult.getIntent())) {
                            List<TodoResult> todoResults = todoService.getTodoSummary(model.getUserToken(),
                                    todoResult.getEntities().getTime().getStartDate(),
                                    todoResult.getEntities().getTime().getEndDate());
                            if (todoResults.isEmpty()) {
                                sendReply(model.getReplyToken(), "太棒了！您在指定時間範圍內沒有待辦事項。");
                            } else {
                                if (todoResults.size() > 10) {
                                    sendReply(model.getReplyToken(),
                                            "您有 " + todoResults.size() + " 筆待辦事項，請縮小查詢範圍（目前僅支援查詢 10 筆以內）");
                                    return;
                                }
                                String flexMessage = lineFlexMessageBuilder.buildTodoListJson(todoResults);
                                log.info("Flex message: " + flexMessage);
                                sendFlexReplyFromJson(model.getReplyToken(), flexMessage, "Todo List");
                            }
                        } else {
                            reply = "[代辦事項] " + todoResult.getEntities().getTask();
                            sendReply(model.getReplyToken(), reply);
                        }
                    } else {
                        log.warn("ProcessResponse result is null, userId: {}, messageId: {}", model.getUserToken(),
                                model.getMessageId());
                        sendReply(model.getReplyToken(), "[代辦事項] 無法處理您的請求，請稍後再試");
                    }
                }).exceptionally(e -> {
                    log.error("NLP /process error, userId: {}, messageId: {}", model.getUserToken(),
                            model.getMessageId(),
                            e);
                    sendReply(model.getReplyToken(), "[代辦事項] 系統出現些許錯誤，請稍後再試");
                    return null;
                });
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> handleHealthAsync(LineMessage model, String domain) {
        log.info("處理健康 domain: {}, userId: {}, messageId: {}", domain, model.getUserToken(), model.getMessageId());
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
        TextMessage replyText = new TextMessage(message);
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

    // 新增：直接以 FlexMessage 回覆（使用 LINE Messaging SDK 的 FlexMessage model）
    public void sendFlexReply(String replyToken, FlexMessage flex) {
        if (flex == null) {
            log.warn("FlexMessage is null, will not send reply");
            return;
        }
        ReplyMessageRequest replyMessageRequest = new ReplyMessageRequest(
                replyToken,
                List.of(flex),
                false);
        try {
            messagingApiClient.replyMessage(replyMessageRequest).get();
            log.info("Flex reply success, replyToken: {}", replyToken);
        } catch (Exception e) {
            log.error("Flex reply failed, replyToken: {}", replyToken, e);
        }
    }

    // 新增：從 Flex JSON 字串建立 FlexMessage 並回覆（altText 可選）
    public void sendFlexReplyFromJson(String replyToken, String flexJson, String altText) {
        if (flexJson == null || flexJson.isBlank()) {
            sendReply(replyToken, "[系統] 無法取得 Flex 內容");
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // --- START: 這是核心修正 ---
            // 1. 將 JSON 字串反序列化成「FlexContainer」物件，而不是「FlexMessage」
            FlexContainer flexContainer = mapper.readValue(flexJson, FlexContainer.class);

            // 2. 建立一個 FlexMessage，並將剛剛的 container 包進去
            FlexMessage flexMessage = new FlexMessage(
                    altText == null || altText.isBlank() ? "為您查詢到的待辦事項" : altText,
                    flexContainer);
            // --- END: 修正結束 ---

            // 3. 呼叫 sendFlexReply 方法來發送
            sendFlexReply(replyToken, flexMessage);
        } catch (Exception e) {
            log.error("Failed to parse Flex JSON, fallback to text reply", e);
            sendReply(replyToken, "[系統] 無法解析 Flex 內容，請稍後再試");
        }
    }

    // 保留舊簽名的相容方法（會使用預設 altText）
    public void sendFlexReplyFromJson(String replyToken, String flexJson) {
        sendFlexReplyFromJson(replyToken, flexJson, "代辦清單");
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
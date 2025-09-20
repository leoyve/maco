package com.example.maco.linebot.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import com.example.maco.linebot.model.RouterRequest;
import com.example.maco.linebot.model.RouterResponse;
import com.example.maco.linebot.util.NlpClient;
import com.example.maco.service.domain.DomainHandler;
import com.example.maco.service.domain.DomainKeys;
import com.example.maco.service.user.LineMessageService;
import com.example.maco.service.user.UserService;
import com.example.maco.linebot.model.PostbackParams;
import com.example.maco.domain.dto.LineMessageDto;
import com.example.maco.domain.model.user.LineMessage;
import com.example.maco.infra.jpa.mapper.LineMessageMapper;

@RequiredArgsConstructor
@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final LineMessageService lineMessageService;
    private final UserService userService;
    private final List<DomainHandler> domainHandlerList;
    private Map<String, DomainHandler> registry;
    private final LineService lineService;

    @PostConstruct
    public void initRegistry() {
        registry = domainHandlerList.stream().collect(Collectors.toMap(DomainHandler::getDomainKey, h -> h));
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> handleTextMessage(LineMessageDto lineMessageDto) {
        lineMessageService.saveMessage(LineMessageMapper.toDomain(lineMessageDto));
        LineMessage lineMessage = LineMessageMapper.toDomain(lineMessageDto);
        userService.upsertUser(lineMessage.getUserToken());

        return NlpClient
                .callNlpApiAsync("/router", new RouterRequest(lineMessage.getMessage()), RouterResponse.class)
                .timeout(Duration.ofSeconds(10))
                .toFuture()
                .thenAccept(routerRes -> {
                    String domain = routerRes != null ? routerRes.getDomain() : DomainKeys.UNKNOWN;
                    log.info("NLPService 判斷 domain: {}, userId: {}, messageId: {}", domain, lineMessage.getUserToken(),
                            lineMessage.getMessageId());
                    DomainHandler handler = registry.get(domain);
                    if (handler != null) {
                        handler.handleAsync(lineMessage, domain)
                                .exceptionally(e -> {
                                    log.error("handleAsync error", e);
                                    return null;
                                });
                    } else {
                        log.error("No DomainHandler found for domain: {}", domain);
                        lineService.sendReply(lineMessage.getReplyToken(), "未知的請求，請嘗試：\n1. 新增代辦事項\n2. 新增體重\n");
                    }
                }).exceptionally(e -> {
                    log.error("NLP /router error, userId: {}, messageId: {}", lineMessage.getUserToken(),
                            lineMessage.getMessageId(), e);
                    lineService.sendReply(lineMessage.getReplyToken(), "系統出現些許錯誤，請稍後再試");
                    return null;
                });
    }

    public void dispatchHandler(LineMessageDto dto) {
        String model = dto.getPostbackParams().get(PostbackParams.MODEL);
        if (DomainKeys.UNKNOWN.equals(model) || model == null || model.isBlank()) {
            lineService.sendReply(dto.getReplyToken(), "系統出現些許錯誤，請稍後再試");
            return;
        }
        DomainHandler handler = registry.get(model);
        if (handler != null) {
            handler.handlePostback(dto)
                    .exceptionally(e -> {
                        log.error("Postback handler failed for params={}", dto.getPostbackParams(), e);
                        try {
                            lineService.sendReply(dto.getReplyToken(), "處理失敗，請稍後再試");
                        } catch (Exception ex) {
                            log.error("Failed to send fallback reply", ex);
                        }
                        return null;
                    });
        } else {
            log.error("No DomainHandler found for model: {}", model);
            lineService.sendReply(dto.getReplyToken(), "系統出現些許錯誤，請稍後再試");
        }
    }

}
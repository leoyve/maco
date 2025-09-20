package com.example.maco.linebot.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.messaging.model.PushMessageRequest;
import com.linecorp.bot.messaging.model.FlexContainer;
import com.linecorp.bot.messaging.model.FlexMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LineService {

    private static final Logger log = LoggerFactory.getLogger(LineService.class);

    private final MessagingApiClient messagingApiClient;
    private final ObjectMapper mapper;

    public String echoMessage(String message) {
        return message;
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
        log.info("sendFlexReply Start");
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
            log.info("sendFlexReply End");
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
            FlexContainer flexContainer = mapper.readValue(flexJson, FlexContainer.class);
            FlexMessage flexMessage = new FlexMessage(
                    altText == null || altText.isBlank() ? "為您查詢到的待辦事項" : altText,
                    flexContainer);
            sendFlexReply(replyToken, flexMessage);
        } catch (Exception e) {
            log.error("Failed to parse Flex JSON, fallback to text reply", e);
            sendReply(replyToken, "[系統] 無法解析 Flex 內容，請稍後再試");
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
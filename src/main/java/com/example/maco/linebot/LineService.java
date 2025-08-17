package com.example.maco.linebot;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.maco.linebot.model.LineMessageDto;
import com.example.maco.adapters.db.jpa.LineUserMessageMapper;
import com.example.maco.adapters.db.jpa.LineUserMessageRepository;
import com.example.maco.adapters.db.jpa.LineUserMessage;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.messaging.model.PushMessageRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void handleTextMessage(LineMessageDto lineMessageDto) {
        // 先將訊息存進DB
        LineUserMessage entity = LineUserMessageMapper.toEntity(lineMessageDto);
        lineUserMessageRepository.save(entity);
        lineUserMessageRepository.flush();

        List<LineUserMessage> messages = lineUserMessageRepository.findByUserId(lineMessageDto.getUserId());
        // 回覆訊息
        StringBuilder sb = new StringBuilder();
        for (LineUserMessage message : messages) {
            sb.append(message.getMessage()).append(message.getReceiveTime()).append("\n");
        }

        log.info("Replying to user {} with messages: {}", lineMessageDto.getUserId(), sb.toString());
        // pushMessage(entity.getUserId(), sb.toString());
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
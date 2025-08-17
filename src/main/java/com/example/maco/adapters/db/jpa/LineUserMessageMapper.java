package com.example.maco.adapters.db.jpa;

import java.time.LocalDateTime;

import com.example.maco.linebot.model.LineMessageDto;

public class LineUserMessageMapper {
    public static LineUserMessage toEntity(String userId, String message, LocalDateTime receiveTime, String type,
            String replyToken, String messageId) {
        return new LineUserMessage(userId, message, receiveTime, type, replyToken, messageId);
    }

    public static LineUserMessage toEntity(LineMessageDto dto) {
        return new LineUserMessage(
                dto.getUserId(),
                dto.getMessage(),
                dto.getReceiveTime(),
                dto.getType(),
                dto.getReplyToken(),
                dto.getMessageId());
    }
}

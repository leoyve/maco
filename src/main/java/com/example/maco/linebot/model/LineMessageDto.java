package com.example.maco.linebot.model;

import java.time.LocalDateTime;

public class LineMessageDto {
    private String userId;
    private String message;
    private LocalDateTime receiveTime;
    private String type;
    private String replyToken;
    private String messageId;

    public LineMessageDto() {
    }

    public LineMessageDto(String userId, String message, LocalDateTime receiveTime, String type, String replyToken,
            String messageId) {
        this.userId = userId;
        this.message = message;
        this.receiveTime = receiveTime;
        this.type = type;
        this.replyToken = replyToken;
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReplyToken() {
        return replyToken;
    }

    public void setReplyToken(String replyToken) {
        this.replyToken = replyToken;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}

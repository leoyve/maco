package com.example.maco.domain.model.user;

import java.time.LocalDateTime;

public class LineMessage {
    private String userToken;
    private String message;
    private LocalDateTime receiveTime;
    private String type;
    private String replyToken;
    private String messageId;

    public LineMessage(String userToken, String message, LocalDateTime receiveTime, String type, String replyToken,
            String messageId) {
        this.userToken = userToken;
        this.message = message;
        this.receiveTime = receiveTime;
        this.type = type;
        this.replyToken = replyToken;
        this.messageId = messageId;
    }

    public String getUserToken() {
        return userToken;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }

    public String getType() {
        return type;
    }

    public String getReplyToken() {
        return replyToken;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setUserToken(String userId) {
        this.userToken = userId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setReplyToken(String replyToken) {
        this.replyToken = replyToken;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

}

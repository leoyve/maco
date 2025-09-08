package com.example.maco.domain.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class LineMessageDto {
    private String userToken;
    private String message;
    private LocalDateTime receiveTime;
    private String type;
    private String replyToken;
    private String messageId;
    private Map<String, String> postbackParams;

    public LineMessageDto() {
    }

    public LineMessageDto(String userToken, String message, LocalDateTime receiveTime, String type, String replyToken,
            String messageId, Map<String, String> postbackParams) {
        this.userToken = userToken;
        this.message = message;
        this.receiveTime = receiveTime;
        this.type = type;
        this.replyToken = replyToken;
        this.messageId = messageId;
        this.postbackParams = postbackParams;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userId) {
        this.userToken = userId;
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

    public Map<String, String> getPostbackParams() {
        return postbackParams;
    }

    public void setPostbackParams(Map<String, String> postbackParams) {
        this.postbackParams = postbackParams;
    }
}

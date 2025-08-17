package com.example.maco.adapters.db.jpa;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "line_user_message")
public class LineUserMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "receive_time", nullable = false)
    private LocalDateTime receiveTime;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "reply_token", length = 50)
    private String replyToken;

    @Column(name = "message_id", length = 50)
    private String messageId;

    // getter, setter, constructor
    public LineUserMessage() {
    }

    public LineUserMessage(String userId, String message, LocalDateTime receiveTime, String type, String replyToken,
            String messageId) {
        this.userId = userId;
        this.message = message;
        this.receiveTime = receiveTime;
        this.type = type;
        this.replyToken = replyToken;
        this.messageId = messageId;
    }

    public Long getId() {
        return id;
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

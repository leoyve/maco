package com.example.maco.infra.jpa.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "line_user_message")
public class LineMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "receive_time", nullable = false, updatable = false)
    private LocalDateTime receiveTime;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "reply_token", length = 50)
    private String replyToken;

    @Column(name = "message_id", length = 50)
    private String messageId;

    public LineMessageEntity() {
        // Default constructor for JPA
    }

    public LineMessageEntity(String userId, String message, LocalDateTime receiveTime, String type, String replyToken,
            String messageId) {
        this.userId = userId;
        this.message = message;
        this.receiveTime = receiveTime;
        this.type = type;
        this.replyToken = replyToken;
        this.messageId = messageId;
    }

}

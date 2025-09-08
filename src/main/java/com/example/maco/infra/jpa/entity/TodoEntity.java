package com.example.maco.infra.jpa.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

@Getter
@Setter
@Entity
@Table(name = "todos")
@EntityListeners(AuditingEntityListener.class)
public class TodoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_token", nullable = false, length = 100)
    private String userToken;

    @Column(name = "task", nullable = false, length = 200)
    private String task;

    @Column(name = "location", nullable = true, length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "original_string", columnDefinition = "TEXT", nullable = true)
    private String originalString;

    @Column(name = "todo_time")
    private Instant todoTime;

    @Column(name = "finish_time")
    private Instant finishTime;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum Status {
        TODO,
        DONE,
        CANCELLED
    }
}

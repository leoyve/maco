package com.example.maco.domain.model.user;

import java.time.Instant;

public class User {
    private final String token;
    private Long groupId;
    private Instant lastUseTime;

    public User(String token, Long groupId, Instant lastUseTime) {
        this.token = token;
        this.groupId = groupId;
        this.lastUseTime = lastUseTime;
    }

    public static User newUser(String token) {
        return new User(token, null, Instant.now());
    }

    public void touch() {
        this.lastUseTime = Instant.now();
    }

    public String getToken() {
        return token;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Instant getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(Instant lastUseTime) {
        this.lastUseTime = lastUseTime;
    }
}

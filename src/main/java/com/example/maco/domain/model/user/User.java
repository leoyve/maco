package com.example.maco.domain.model.user;

import java.time.Instant;

public class User {
    private final String userToken;
    private Long groupId;
    private Instant lastUseTime;

    public User(String userToken, Long groupId, Instant lastUseTime) {
        this.userToken = userToken;
        this.groupId = groupId;
        this.lastUseTime = lastUseTime;
    }

    public static User newUser(String userToken) {
        return new User(userToken, null, Instant.now());
    }

    public void touch() {
        this.lastUseTime = Instant.now();
    }

    public String getUserToken() {
        return userToken;
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

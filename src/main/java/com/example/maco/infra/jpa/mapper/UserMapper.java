package com.example.maco.infra.jpa.mapper;

import com.example.maco.domain.model.user.User;
import com.example.maco.infra.jpa.entity.UserEntity;

// infra/jpa/mapper/UserMapper.java
public final class UserMapper {
    private UserMapper() {
    }

    public static User toModel(UserEntity e) {
        if (e == null) {
            return null;
        }
        return new User(e.getUserToken(), e.getGroupId(), e.getLastUseTime());
    }

    public static UserEntity toEntity(User m) {
        var e = new UserEntity();
        e.setUserToken(m.getUserToken());
        e.setGroupId(m.getGroupId());
        e.setLastUseTime(m.getLastUseTime());
        return e;
    }

    public static void copyToEntity(User m, UserEntity e) {
        e.setGroupId(m.getGroupId());
        e.setLastUseTime(m.getLastUseTime());
    }
}

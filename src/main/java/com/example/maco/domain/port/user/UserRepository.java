package com.example.maco.domain.port.user;

import java.util.List;
import java.util.Optional;

import com.example.maco.domain.model.user.User;

public interface UserRepository {
    Optional<User> findByToken(String token);

    List<User> findGroupMembersByToken(String token, boolean includeSelf);

    void save(User user); // saveOrUpdate
}

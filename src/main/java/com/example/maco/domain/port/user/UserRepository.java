package com.example.maco.domain.port.user;

import java.util.Optional;

import com.example.maco.domain.model.user.User;

public interface UserRepository {
    Optional<User> findByToken(String token);

    void save(User user); // saveOrUpdate
}

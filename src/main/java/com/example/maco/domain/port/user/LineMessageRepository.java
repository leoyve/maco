package com.example.maco.domain.port.user;

import java.util.List;

import com.example.maco.domain.model.user.LineMessage;

public interface LineMessageRepository {

    void save(LineMessage message);

    List<LineMessage> findByUserToken(String userId);
}

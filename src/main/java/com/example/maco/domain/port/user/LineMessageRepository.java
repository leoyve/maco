package com.example.maco.domain.port.user;

import com.example.maco.domain.model.user.LineMessage;

public interface LineMessageRepository {

    void save(LineMessage message);
}

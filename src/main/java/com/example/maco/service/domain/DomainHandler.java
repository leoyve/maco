package com.example.maco.service.domain;

import java.util.concurrent.CompletableFuture;

import com.example.maco.domain.dto.LineMessageDto;
import com.example.maco.domain.model.user.LineMessage;

public interface DomainHandler {

    CompletableFuture<Void> handleAsync(LineMessage model, String domain);

    CompletableFuture<Void> handlePostback(LineMessageDto lineMessageDto);

    String getDomainKey();
}

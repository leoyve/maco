package com.example.maco.user;

import org.springframework.stereotype.Service;

import com.example.maco.domain.model.user.LineMessage;
import com.example.maco.domain.port.user.LineMessageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LineMessageService {

    private final LineMessageRepository lineMessageRepo;

    @Transactional
    public void saveMessage(LineMessage model) {
        lineMessageRepo.save(model);
    }
}

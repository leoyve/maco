package com.example.maco.user;

import org.springframework.stereotype.Service;

import com.example.maco.domain.model.user.User;
import com.example.maco.domain.port.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;

    @Transactional
    public void upsertUser(String userId) {
        var model = userRepo.findByToken(userId)
                .orElse(User.newUser(userId));
        model.touch();
        userRepo.save(model); // ← 傳 Model，轉換發生在 Adapter
    }
}

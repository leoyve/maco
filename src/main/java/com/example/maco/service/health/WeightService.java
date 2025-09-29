package com.example.maco.service.health;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.maco.domain.model.health.WeightResult;
import com.example.maco.domain.port.user.WeightRepository;
import com.example.maco.infra.exception.DomainException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeightService {
    private final WeightRepository weightRepo;

    @Transactional
    public void insertWeight(String userToken, WeightResult model) {
        // 基本參數驗證
        if (model == null) {
            throw new DomainException("WeightResult must not be null");
        }
        var entities = model.getEntities();
        if (entities == null) {
            throw new DomainException("Weight entities are required");
        }

        BigDecimal weight = entities.getWeight();
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Weight is required for addWeight intent");
        }
        // 通過驗證後交由 repo 保存（adapter 會做轉換與 infra 包裝）
        weightRepo.save(userToken, model); // ← 傳 Model，轉換發生在 Adapter
    }

}

package com.example.maco.infra.jpa.adapter;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.maco.domain.model.user.LineMessage;
import com.example.maco.domain.port.user.LineMessageRepository;
import com.example.maco.infra.jpa.mapper.LineMessageMapper;
import com.example.maco.infra.jpa.repo.LineMessageJpaRepo;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaLineMessageRepository implements LineMessageRepository {

    private final LineMessageJpaRepo repo;

    @Override
    public List<LineMessage> findByUserToken(String userId) {
        return repo.findByUserId(userId)
                .stream()
                .map(LineMessageMapper::toDomain)
                .toList();
    }

    @Override
    public void save(LineMessage message) {
        repo.save(LineMessageMapper.toEntity(message));
    }
}

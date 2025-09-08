package com.example.maco.infra.jpa.adapter;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.example.maco.domain.model.user.LineMessage;
import com.example.maco.domain.port.user.LineMessageRepository;
import com.example.maco.infra.exception.InfraException;
import com.example.maco.infra.jpa.mapper.LineMessageMapper;
import com.example.maco.infra.jpa.repo.LineMessageJpaRepo;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaLineMessageRepository implements LineMessageRepository {

    private final LineMessageJpaRepo repo;

    @Override
    public void save(LineMessage message) {
        try {
            repo.save(LineMessageMapper.toEntity(message));
        } catch (DataAccessException e) {
            throw new InfraException(
                    "Failed to save line message for user: " + (message != null ? message.getUserToken() : "null"), e);
        }
    }
}

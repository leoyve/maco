package com.example.maco.infra.jpa.adapter;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.domain.port.user.TodoRepository;
import com.example.maco.infra.exception.InfraException;
import com.example.maco.infra.jpa.entity.TodoEntity;
import com.example.maco.infra.jpa.mapper.TodoMapper;
import com.example.maco.infra.jpa.repo.TodoJpaRepo;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaTodoRepository implements TodoRepository {

    private final TodoJpaRepo repo;

    @Override
    public void save(TodoResult result) {
        try {
            TodoEntity entity = TodoMapper.toEntity(result);
            repo.save(entity);
        } catch (DataAccessException e) {
            throw new InfraException("Failed to save todo result", e);
        }
    }
}

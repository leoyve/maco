package com.example.maco.infra.jpa.adapter;

import org.springframework.stereotype.Repository;

import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.domain.port.user.TodoRepository;
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
        TodoEntity entity = TodoMapper.toEntity(result);
        repo.save(entity);
    }
}

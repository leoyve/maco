package com.example.maco.infra.jpa.adapter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<TodoResult> findTodoByTimeRange(Instant start, Instant end) {
        try {
            List<TodoEntity> entities = repo.findByStatusAndTodoTimeBetweenOrderByTodoTimeAsc(
                    TodoEntity.Status.TODO, start, end);
            return entities.stream().map(TodoMapper::toDomain).collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new InfraException("Failed to query todo by status and time range", e);
        }
    }
}

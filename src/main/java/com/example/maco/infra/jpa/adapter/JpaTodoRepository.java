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
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class JpaTodoRepository implements TodoRepository {

    private final TodoJpaRepo repo;

    @Override
    public void save(String userToken, TodoResult result) {
        try {
            TodoEntity entity = TodoMapper.toEntity(result);
            entity.setUserToken(userToken);
            repo.save(entity);
        } catch (DataAccessException e) {
            throw new InfraException("Failed to save todo result", e);
        }
    }

    @Override
    public List<TodoResult> findTodoByTimeRange(String userToken, Instant start, Instant end) {
        try {
            log.info("findTodoByTimeRange start");
            List<TodoEntity> entities = repo.findByUserTokenAndTodoTime(userToken, start, end);
            log.info("findTodoByTimeRange end");
            return entities.stream().map(TodoMapper::toDomain).collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new InfraException("Failed to query todo by status and time range", e);
        }
    }

    public int deleteById(String userToken, Long todoId) {
        try {
            return repo.deleteByUserTokenAndId(userToken, todoId);
        } catch (DataAccessException e) {
            throw new InfraException("Failed to delete todo by ID", e);
        }
    }

    public int completeById(String userToken, Long todoId, Instant finishTime) {
        try {
            return repo.completeByUserTokenAndId(userToken, todoId, finishTime);
        } catch (DataAccessException e) {
            throw new InfraException("Failed to complete todo by ID", e);
        }
    }
}

package com.example.maco.infra.jpa.mapper;

import java.time.Instant;

import com.example.maco.domain.dto.TodoResultDto;
import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.infra.jpa.entity.TodoEntity;
import com.example.maco.infra.jpa.util.DateTimeUtils;

public final class TodoMapper {
    public static TodoEntity toEntity(TodoResult todoResult) {
        if (todoResult == null) {
            return null;
        }
        TodoEntity entity = new TodoEntity();
        if (todoResult.getEntities() != null) {
            entity.setTask(todoResult.getEntities().getTask());
            if (todoResult.getEntities().getTime() != null) {
                String ts = todoResult.getEntities().getTime().getTimestamp();
                if (ts != null && !ts.isEmpty()) {
                    // 使用共用工具解析時間字串
                    Instant parsed = DateTimeUtils.parseToInstant(ts);
                    entity.setTodoTime(parsed);
                }
            }
            entity.setLocation(todoResult.getEntities().getLocation());
            entity.setStatus(TodoEntity.Status.valueOf(todoResult.getEntities().getStatus()));
        }
        entity.setOriginalString(todoResult.toString());
        entity.setUserToken(todoResult.getUserToken());
        return entity;
    }

    public static TodoResult toDomain(TodoEntity e) {
        if (e == null)
            return null;

        TodoResult.TodoEntities entities = new TodoResult.TodoEntities();
        entities.setTask(e.getTask());
        entities.setLocation(e.getLocation());
        entities.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        if (e.getTodoTime() != null) {
            TodoResult.TodoEntities.TodoTime time = new TodoResult.TodoEntities.TodoTime();
            time.setTimestamp(e.getTodoTime().toString());
            entities.setTime(time);
        }
        TodoResult result = new TodoResult();
        result.setUserToken(e.getUserToken());
        result.setId(e.getId());
        result.setEntities(entities);
        result.setIntent(null);
        result.setDomain(null);
        result.setClear(true);
        result.setRecommendation(null);
        return result;
    }

    public static TodoResult toDomain(TodoResultDto dto) {
        if (dto == null)
            return null;
        TodoResult.TodoEntities entities = null;
        if (dto.getEntities() != null) {
            TodoResult.TodoEntities.TodoTime time = null;
            if (dto.getEntities().getTime() != null) {
                time = new TodoResult.TodoEntities.TodoTime();
                time.setTimestamp(dto.getEntities().getTime().getTimestamp());
                time.setStartDate(dto.getEntities().getTime().getStartDate());
                time.setEndDate(dto.getEntities().getTime().getEndDate());
            }
            entities = new TodoResult.TodoEntities();
            entities.setTask(dto.getEntities().getTask());
            entities.setTime(time);
            entities.setLocation(dto.getEntities().getLocation());
            entities.setStatus(dto.getEntities().getStatus());
        }
        TodoResult result = new TodoResult();
        result.setUserToken(dto.getUserToken());
        result.setIntent(dto.getIntent());
        result.setEntities(entities);
        result.setClear(dto.isClear());
        result.setRecommendation(dto.getRecommendation());
        result.setDomain(dto.getDomain());
        return result;
    }
}
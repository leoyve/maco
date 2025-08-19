package com.example.maco.infra.jpa.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.example.maco.domain.dto.TodoResultDto;
import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.infra.jpa.entity.TodoEntity;

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
                    try {
                        // 支援 "yyyy-MM-dd HH:mm" 格式
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        LocalDateTime ldt = LocalDateTime.parse(ts, dtf);
                        entity.setTodoTime(ldt.atZone(ZoneId.systemDefault()).toInstant());
                    } catch (Exception e) {
                        // 若 parse 失敗，嘗試 ISO 格式
                        try {
                            entity.setTodoTime(Instant.parse(ts));
                        } catch (Exception ex) {
                            entity.setTodoTime(null); // 解析失敗
                        }
                    }
                }
            }
            entity.setLocation(todoResult.getEntities().getLocation());
            entity.setStatus(TodoEntity.Status.valueOf(todoResult.getEntities().getStatus()));
        }
        entity.setOriginalString(todoResult.toString());
        return entity;
    }

    // public static TodoResult toDomain(LineMessageEntity entity) {
    // if (entity == null) {
    // return null;
    // }
    // return new LineMessage(entity.getUserId(), entity.getMessage(),
    // entity.getReceiveTime(),
    // entity.getType(), entity.getReplyToken(), entity.getMessageId());
    // }

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
        result.setIntent(dto.getIntent());
        result.setEntities(entities);
        result.setClear(dto.isClear());
        result.setRecommendation(dto.getRecommendation());
        result.setDomain(dto.getDomain());
        return result;
    }
}
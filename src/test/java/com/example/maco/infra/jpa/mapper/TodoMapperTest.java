package com.example.maco.infra.jpa.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.example.maco.domain.model.todo.TodoResult;

public class TodoMapperTest {

    @Test
    void toEntity_shouldParseCustomPattern_timestampPresent() {
        TodoResult todo = new TodoResult();
        TodoResult.TodoEntities entities = new TodoResult.TodoEntities();
        TodoResult.TodoEntities.TodoTime time = new TodoResult.TodoEntities.TodoTime();
        time.setTimestamp("2025-08-22 00:00");
        entities.setTime(time);
        entities.setTask("task");
        entities.setLocation("loc");
        entities.setStatus("TODO");
        todo.setEntities(entities);

        var entity = TodoMapper.toEntity(todo);
        assertNotNull(entity);
        assertNotNull(entity.getTodoTime(), "自訂格式應被解析為非空 Instant");
    }

    @Test
    void toEntity_shouldParseIsoTimestamp_timestampPresent() {
        TodoResult todo = new TodoResult();
        TodoResult.TodoEntities entities = new TodoResult.TodoEntities();
        TodoResult.TodoEntities.TodoTime time = new TodoResult.TodoEntities.TodoTime();
        time.setTimestamp("2025-08-22T00:00:00Z");
        entities.setTime(time);
        entities.setTask("task");
        entities.setLocation("loc");
        entities.setStatus("TODO");
        todo.setEntities(entities);

        var entity = TodoMapper.toEntity(todo);
        assertNotNull(entity);
        Instant inst = entity.getTodoTime();
        assertNotNull(inst, "ISO 格式應被解析為非空 Instant");
        assertEquals(Instant.parse("2025-08-22T00:00:00Z"), inst);
    }

    @Test
    void toEntity_invalidTimestamp_todoTimeNull() {
        TodoResult todo = new TodoResult();
        TodoResult.TodoEntities entities = new TodoResult.TodoEntities();
        TodoResult.TodoEntities.TodoTime time = new TodoResult.TodoEntities.TodoTime();
        time.setTimestamp("not-a-timestamp");
        entities.setTime(time);
        entities.setTask("task");
        entities.setLocation("loc");
        entities.setStatus("TODO");
        todo.setEntities(entities);

        var entity = TodoMapper.toEntity(todo);
        assertNotNull(entity);
        assertNull(entity.getTodoTime(), "無法解析的時間應回傳 null");
    }
}

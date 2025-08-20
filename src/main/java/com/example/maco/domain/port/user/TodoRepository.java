package com.example.maco.domain.port.user;

import java.time.Instant;
import java.util.List;

import com.example.maco.domain.model.todo.TodoResult;

public interface TodoRepository {
    void save(TodoResult result); // saveOrUpdate

    List<TodoResult> findTodoByTimeRange(Instant start, Instant end);

}

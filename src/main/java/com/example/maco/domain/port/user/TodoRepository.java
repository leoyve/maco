package com.example.maco.domain.port.user;

import java.time.Instant;
import java.util.List;

import com.example.maco.domain.model.todo.TodoResult;

public interface TodoRepository {
    void save(String userToken, TodoResult result); // saveOrUpdate

    List<TodoResult> findTodoByTimeRange(String userToken, Instant start, Instant end);

}

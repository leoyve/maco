package com.example.maco.domain.port.user;

import com.example.maco.domain.model.todo.TodoResult;

public interface TodoRepository {
    void save(TodoResult result); // saveOrUpdate
}

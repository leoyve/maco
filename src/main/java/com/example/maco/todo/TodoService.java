package com.example.maco.todo;

import org.springframework.stereotype.Service;

import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.domain.port.user.TodoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepo;

    @Transactional
    public void insertTodo(TodoResult model) {
        todoRepo.save(model); // ← 傳 Model，轉換發生在 Adapter
    }
}

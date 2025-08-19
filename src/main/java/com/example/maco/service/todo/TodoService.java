package com.example.maco.service.todo;

import org.springframework.stereotype.Service;

import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.domain.port.user.TodoRepository;
import com.example.maco.infra.exception.DomainException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepo;

    @Transactional
    public void insertTodo(TodoResult model) {
        // 基本參數驗證
        if (model == null) {
            throw new DomainException("TodoResult must not be null");
        }
        var entities = model.getEntities();
        if (entities == null) {
            throw new DomainException("Todo entities are required");
        }

        // 若為新增代辦，task 必填
        if ("addTodo".equals(model.getIntent())) {
            String task = entities.getTask();
            if (task == null || task.isBlank()) {
                throw new DomainException("Task is required for addTodo intent");
            }
        }
        // 檢查狀態
        String status = entities.getStatus();
        if (status == null || !status.strip().equalsIgnoreCase("TODO")) {
            throw new DomainException("Status must be TODO");
        }
        // 通過驗證後交由 repo 保存（adapter 會做轉換與 infra 包裝）
        todoRepo.save(model); // ← 傳 Model，轉換發生在 Adapter
    }
}

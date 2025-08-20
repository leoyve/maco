package com.example.maco.infra.jpa.repo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.maco.infra.jpa.entity.TodoEntity;
import com.example.maco.infra.jpa.entity.TodoEntity.Status;

public interface TodoJpaRepo extends JpaRepository<TodoEntity, Long> {

        // 依狀態且在時間區間內（含邊界），依 todoTime 升序
        List<TodoEntity> findByStatusAndTodoTimeBetweenOrderByTodoTimeAsc(Status status, Instant start, Instant end);
}

package com.example.maco.infra.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.maco.infra.jpa.entity.TodoEntity;

public interface TodoJpaRepo extends JpaRepository<TodoEntity, Long> {
}

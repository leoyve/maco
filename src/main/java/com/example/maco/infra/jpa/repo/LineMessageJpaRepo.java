package com.example.maco.infra.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.maco.infra.jpa.entity.LineMessageEntity;

public interface LineMessageJpaRepo extends JpaRepository<LineMessageEntity, Long> {
}

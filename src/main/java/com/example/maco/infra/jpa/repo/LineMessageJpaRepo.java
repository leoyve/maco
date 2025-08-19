package com.example.maco.infra.jpa.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.maco.infra.jpa.entity.LineMessageEntity;

public interface LineMessageJpaRepo extends JpaRepository<LineMessageEntity, Long> {

        @Query("SELECT l FROM LineMessageEntity l WHERE l.userId = :userId")
        List<LineMessageEntity> findByUserId(String userId);
}

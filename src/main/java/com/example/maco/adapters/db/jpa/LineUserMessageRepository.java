package com.example.maco.adapters.db.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineUserMessageRepository extends JpaRepository<LineUserMessage, Long> {

    // 可自訂查詢方法，例如：
    List<LineUserMessage> findByUserId(String userId);
}

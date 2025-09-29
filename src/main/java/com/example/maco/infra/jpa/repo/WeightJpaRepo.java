package com.example.maco.infra.jpa.repo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.maco.infra.jpa.entity.WeightEntity;

public interface WeightJpaRepo extends JpaRepository<WeightEntity, Long> {

        // 依 userToken，且狀態固定為 'TODO'；在時間區間內（含邊界）或 todoTime 為 null，依 todoTime 升序
        // @Query("SELECT t FROM WeightEntity t "
        // + "WHERE t.userToken = :userToken "
        // + "AND (t.todoTime BETWEEN :start AND :end OR t.todoTime IS NULL) "
        // + "ORDER BY CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END ASC, t.todoTime
        // ASC")
        // List<WeightEntity> findByUserTokenAndTodoTime(@Param("userToken") String
        // userToken,
        // @Param("start") Instant start,
        // @Param("end") Instant end);

}
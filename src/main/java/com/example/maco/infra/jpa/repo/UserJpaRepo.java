package com.example.maco.infra.jpa.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.maco.infra.jpa.entity.UserEntity;

public interface UserJpaRepo extends JpaRepository<UserEntity, String> {

    // 同群查詢（子查詢版）
    @Query("""
            SELECT u2 FROM UserEntity u2
            WHERE u2.groupId = (SELECT u.groupId FROM UserEntity u WHERE u.token = :token)
            """)
    List<UserEntity> findGroupMembersByToken(@Param("token") String token);

    // 排除自己版本
    @Query("""
            SELECT u2 FROM UserEntity u2
            WHERE u2.groupId = (SELECT u.groupId FROM UserEntity u WHERE u.token = :token)
              AND u2.token <> :token
            """)
    List<UserEntity> findOtherGroupMembersByToken(@Param("token") String token);
}

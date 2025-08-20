package com.example.maco.infra.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.maco.infra.jpa.entity.UserEntity;

public interface UserJpaRepo extends JpaRepository<UserEntity, String> {

}

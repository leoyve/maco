package com.example.maco.infra.jpa.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.example.maco.domain.model.user.User;
import com.example.maco.domain.port.user.UserRepository;
import com.example.maco.infra.exception.InfraException;
import com.example.maco.infra.jpa.mapper.UserMapper;
import com.example.maco.infra.jpa.repo.UserJpaRepo;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final UserJpaRepo repo;

    @Override
    public Optional<User> findByToken(String token) {
        try {
            return repo.findById(token).map(UserMapper::toModel);
        } catch (DataAccessException e) {
            throw new InfraException("Failed to find user by token: " + token, e);
        }
    }

    @Override
    public List<User> findGroupMembersByToken(String token, boolean includeSelf) {
        try {
            var list = includeSelf
                    ? repo.findGroupMembersByToken(token)
                    : repo.findOtherGroupMembersByToken(token);
            return list.stream().map(UserMapper::toModel).toList();
        } catch (DataAccessException e) {
            throw new InfraException("Failed to find group members for token: " + token, e);
        }
    }

    @Override
    public void save(User user) {
        try {
            // Spring Data JPA 的 save() = saveOrUpdate
            // 這裡決定「新建或更新」的欄位行為
            var entity = repo.findById(user.getToken())
                    .orElseGet(() -> UserMapper.toEntity(user)); // INSERT 走這
            if (entity.getToken() != null) {
                // UPDATE：用 Model 覆蓋到已存在的 Entity
                UserMapper.copyToEntity(user, entity);
            }
            repo.save(entity);
        } catch (DataAccessException e) {
            throw new InfraException("Failed to save user: " + (user != null ? user.getToken() : "null"), e);
        }
    }
}

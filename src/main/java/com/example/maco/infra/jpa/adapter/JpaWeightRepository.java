package com.example.maco.infra.jpa.adapter;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.example.maco.domain.model.health.WeightResult;
import com.example.maco.domain.port.user.WeightRepository;
import com.example.maco.infra.exception.InfraException;
import com.example.maco.infra.jpa.entity.WeightEntity;
import com.example.maco.infra.jpa.mapper.WeightMapper;
import com.example.maco.infra.jpa.repo.WeightJpaRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class JpaWeightRepository implements WeightRepository {

    private final WeightJpaRepo repo;

    @Override
    public void save(String userToken, WeightResult result) {
        try {
            WeightEntity entity = WeightMapper.toEntity(result);
            entity.setUserToken(userToken);
            repo.save(entity);
        } catch (DataAccessException e) {
            throw new InfraException("Failed to save weight result", e);
        }
    }
}

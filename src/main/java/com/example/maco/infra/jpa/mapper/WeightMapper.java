package com.example.maco.infra.jpa.mapper;

import java.time.Instant;

import com.example.maco.domain.dto.WeightResultDto;
import com.example.maco.domain.model.health.WeightResult;
import com.example.maco.infra.jpa.entity.WeightEntity;
import com.example.maco.infra.jpa.util.DateTimeUtils;

public final class WeightMapper {
    public static WeightEntity toEntity(WeightResult healthResult) {
        if (healthResult == null) {
            return null;
        }
        WeightEntity entity = new WeightEntity();
        if (healthResult.getEntities() != null) {
            entity.setWeight(healthResult.getEntities().getWeight());
            if (healthResult.getEntities().getTime() != null) {
                String ts = healthResult.getEntities().getTime().getTimestamp();
                if (ts != null && !ts.isEmpty()) {
                    // 使用共用工具解析時間字串
                    Instant parsed = DateTimeUtils.parseToInstant(ts);
                    entity.setRecordAt(parsed);
                }
            }
        }
        return entity;
    }

    public static WeightResult toDomain(WeightEntity e) {
        if (e == null)
            return null;

        WeightResult.HealthEntities entities = new WeightResult.HealthEntities();
        entities.setWeight(e.getWeight());
        if (e.getRecordAt() != null) {
            WeightResult.HealthEntities.HealthTime time = new WeightResult.HealthEntities.HealthTime();
            time.setTimestamp(e.getRecordAt().toString());
            entities.setTime(time);
        }
        WeightResult result = new WeightResult();
        result.setId(e.getId());
        result.setEntities(entities);
        result.setIntent(null);
        result.setDomain(null);
        result.setClear(true);
        result.setRecommendation(null);
        return result;
    }

    public static WeightResult toDomain(WeightResultDto dto) {
        if (dto == null)
            return null;
        WeightResult.HealthEntities entities = null;
        if (dto.getEntities() != null) {
            WeightResult.HealthEntities.HealthTime time = null;
            if (dto.getEntities().getTime() != null) {
                time = new WeightResult.HealthEntities.HealthTime();
                time.setTimestamp(dto.getEntities().getTime().getTimestamp());
            }
            entities = new WeightResult.HealthEntities();
            entities.setWeight(dto.getEntities().getWeight());
            entities.setTime(time);
        }
        WeightResult result = new WeightResult();
        result.setIntent(dto.getIntent());
        result.setEntities(entities);
        result.setClear(dto.isClear());
        result.setRecommendation(dto.getRecommendation());
        result.setDomain(dto.getDomain());
        return result;
    }
}
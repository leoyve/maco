package com.example.maco.domain.model.health;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

import com.example.maco.domain.model.BaseResult;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeightResult extends BaseResult {

    private Long id;
    private HealthEntities entities;

    @Override
    public String toString() {
        return "HealthResult{" +
                "entities=" + (entities != null ? entities.toString() : "null") +
                ", intent='" + getIntent() + '\'' +
                ", domain='" + getDomain() + '\'' +
                ", isClear=" + isClear() +
                ", recommendation='" + getRecommendation() + '\'' +
                '}';
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthEntities {
        private BigDecimal weight;
        private HealthTime time;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HealthTime {
            private String timestamp;

            @Override
            public String toString() {
                return "HealthTime{" +
                        "timestamp='" + timestamp + '\'' +
                        '}';
            }
        }
    }
}

package com.example.maco.domain.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeightResultDto extends BaseResultDto {

    private HealthEntities entities;

    @Getter
    @Setter
    public static class HealthEntities {
        private BigDecimal weight;
        private HealthTime time;

        @Getter
        @Setter
        public static class HealthTime {
            private String timestamp;
        }
    }
}

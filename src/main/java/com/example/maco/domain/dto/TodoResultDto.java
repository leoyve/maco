package com.example.maco.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoResultDto extends BaseResultDto {

    private TodoEntities entities;

    @Getter
    @Setter
    public static class TodoEntities {
        private String task;
        private TodoTime time;
        private String location;
        private String status;

        @Getter
        @Setter
        public static class TodoTime {
            private String timestamp;
            private String startDate;
            private String endDate;
        }
    }
}

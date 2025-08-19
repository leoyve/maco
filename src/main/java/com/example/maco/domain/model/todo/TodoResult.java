package com.example.maco.domain.model.todo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.example.maco.domain.model.BaseResult;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoResult extends BaseResult {

    private TodoEntities entities;

    @Override
    public String toString() {
        return "TodoResult{" +
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
    public static class TodoEntities {
        private String task;
        private TodoTime time;
        private String location;
        private String status;

        @Override
        public String toString() {
            return "TodoEntities{" +
                    "task='" + task + '\'' +
                    ", time=" + (time != null ? time.toString() : "null") +
                    ", location='" + location + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TodoTime {
            private String timestamp;
            private String startDate;
            private String endDate;

            @Override
            public String toString() {
                return "TodoTime{" +
                        "timestamp='" + timestamp + '\'' +
                        ", startDate='" + startDate + '\'' +
                        ", endDate='" + endDate + '\'' +
                        '}';
            }
        }
    }
}

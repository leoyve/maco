package com.example.maco.domain.model.todo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.example.maco.domain.model.BaseResult;
import com.example.maco.infra.jpa.util.DateTimeUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoResult extends BaseResult {

    private Long id;
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

    // --- domain helper methods for user-facing text ---
    public String getSanitizedTask() {
        if (entities == null || entities.getTask() == null)
            return "(未提供內容)";
        return entities.getTask().trim().replaceAll("[\\r\\n]+", " ");
    }

    public String getTimeSummary() {
        if (entities == null || entities.getTime() == null)
            return "";
        TodoEntities.TodoTime t = entities.getTime();
        if (t == null)
            return "";
        String ts = t.getTimestamp();
        if (ts != null && !ts.isBlank()) {
            return "（" + DateTimeUtils.formatInstantToLocal(ts) + "）";
        }
        return "";
    }

    // 合併 sanitize 與 summary 為單一方法
    public String getLocationSummary() {
        if (entities == null || entities.getLocation() == null)
            return "";
        String loc = entities.getLocation().trim().replaceAll("[\\r\\n]+", " ");
        if (loc.isBlank())
            return "";
        return "（地點：" + loc + "）";
    }

    public String toUserMessageForAdd() {
        return "已新增代辦：「" + getSanitizedTask() + "」" + getTimeSummary() + getLocationSummary();
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

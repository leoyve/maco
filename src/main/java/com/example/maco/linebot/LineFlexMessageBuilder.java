package com.example.maco.linebot;

import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.infra.jpa.util.DateTimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.time.*;

@Component
public class LineFlexMessageBuilder {

    private static final Logger log = LoggerFactory.getLogger(LineFlexMessageBuilder.class);
    private final ObjectMapper objectMapper = new ObjectMapper(); // Reusable ObjectMapper

    // 快取 template 作為 ObjectNode（只讀一次）
    private ObjectNode cachedTodoTemplate;

    @PostConstruct
    private void loadTemplate() {
        try (InputStream is = new ClassPathResource("flex/todo_list_template.json").getInputStream()) {
            String todoTemplateJson = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            cachedTodoTemplate = (ObjectNode) objectMapper.readTree(todoTemplateJson);
        } catch (Exception e) {
            log.error("Failed to load flex template", e);
            cachedTodoTemplate = null;
        }
    }

    public String buildTodoListJson(List<TodoResult> todos) {
        try {
            if (cachedTodoTemplate == null) {
                log.warn("No cached template available");
                return null;
            }

            // work on a deep copy so cachedTemplate 不會被改寫
            ObjectNode templateNode = cachedTodoTemplate.deepCopy();

            ArrayNode contentsNode = (ArrayNode) templateNode.with("body").withArray("contents");
            contentsNode.removeAll();

            for (TodoResult todo : todos) {
                ObjectNode todoBox = createTodoItemBox(todo);
                contentsNode.add(todoBox);
            }

            return objectMapper.writeValueAsString(templateNode);

        } catch (Exception e) {
            log.error("Failed to build TodoList Flex JSON", e);
            return null;
        }
    }

    /**
     * Helper method to create a single to-do item's box component as an ObjectNode.
     * 
     * @param todo The TodoResult object from the database.
     * @return An ObjectNode representing the Flex Message component.
     */
    private ObjectNode createTodoItemBox(TodoResult todo) {
        ObjectNode box = objectMapper.createObjectNode();
        box.put("type", "box");
        box.put("layout", "horizontal");
        box.put("paddingTop", "md");
        box.put("alignItems", "center");

        // safe access to entities
        var ent = todo.getEntities();

        // build components
        ObjectNode statusIconWrapper = buildStatusIconBox(ent != null ? ent.getStatus() : null);
        ObjectNode detailsWrapper = buildDetailsWrapper(todo);
        ObjectNode buttonWrapper = buildButtonWrapper(todo, ent != null ? ent.getStatus() : null);

        ((ArrayNode) box.putArray("contents")).add(statusIconWrapper).add(detailsWrapper).add(buttonWrapper);
        return box;
    }

    private ObjectNode buildStatusIconBox(String status) {
        ObjectNode statusIconContents = objectMapper.createObjectNode();
        statusIconContents.put("type", "text");
        statusIconContents.put("flex", 0);
        boolean done = "DONE".equals(status);
        statusIconContents.put("text", done ? "✓" : "●");
        statusIconContents.put("color", done ? "#1DB446" : "#FFB74D");
        if (done) {
            statusIconContents.put("weight", "bold");
        }

        ObjectNode statusIconWrapper = objectMapper.createObjectNode();
        statusIconWrapper.put("type", "box");
        statusIconWrapper.put("layout", "vertical");
        statusIconWrapper.put("width", "20px");
        statusIconWrapper.put("alignItems", "center");
        ((ArrayNode) statusIconWrapper.putArray("contents")).add(statusIconContents);
        return statusIconWrapper;
    }

    private ObjectNode buildDetailsWrapper(TodoResult todo) {
        var ent = todo.getEntities();
        String task = ent != null && ent.getTask() != null ? ent.getTask() : "未命名待辦";
        String location = ent != null && ent.getLocation() != null ? ent.getLocation() : "";

        // title
        ObjectNode titleText = objectMapper.createObjectNode();
        titleText.put("type", "text");
        titleText.put("text", task);
        titleText.put("weight", "bold");
        titleText.put("wrap", true);
        titleText.put("maxLines", 2);
        if (ent != null && "DONE".equals(ent.getStatus())) {
            titleText.put("decoration", "line-through");
            titleText.put("color", "#0c0808ff");
        }

        // time/location
        ObjectNode timeText = objectMapper.createObjectNode();
        timeText.put("type", "text");
        timeText.put("wrap", true);
        timeText.put("maxLines", 2);
        timeText.put("size", "xs");
        timeText.put("color", "#B2B2B2");

        Instant ts = null;
        if (ent != null && ent.getTime() != null) {
            ts = DateTimeUtils.parseToInstant(ent.getTime().getTimestamp());
        }

        if (ts != null) {
            ZoneId zone = ZoneId.systemDefault();
            String dayLabel = DateTimeUtils.formatFlexDayLabel(ts, zone);
            String timeStr = DateTimeUtils.formatFlexTime(ts, zone);
            String composed = (dayLabel + " " + timeStr).trim();
            if (!location.isBlank()) {
                // place location on new line and omit if empty
                composed = composed + "\n@ " + location;
            }
            timeText.put("text", composed);
        } else if (!location.isBlank()) {
            timeText.put("text", "@ " + location);
        } else {
            timeText.put("text", "未指定時間/地點");
        }

        ObjectNode detailsWrapper = objectMapper.createObjectNode();
        detailsWrapper.put("type", "box");
        detailsWrapper.put("layout", "vertical");
        detailsWrapper.put("flex", 3);
        ((ArrayNode) detailsWrapper.putArray("contents")).add(titleText).add(timeText);
        return detailsWrapper;
    }

    private ObjectNode buildButtonWrapper(TodoResult todo, String status) {
        ObjectNode buttonWrapper = objectMapper.createObjectNode();
        buttonWrapper.put("type", "box");
        buttonWrapper.put("layout", "horizontal");
        buttonWrapper.put("flex", 2);
        buttonWrapper.put("spacing", "sm");

        // only non-DONE show complete button
        if (!"DONE".equals(status)) {
            ObjectNode completeButton = objectMapper.createObjectNode();
            completeButton.put("type", "button");
            completeButton.put("style", "primary");
            completeButton.put("height", "sm");
            completeButton.put("color", "#3399FF");
            ObjectNode completeAction = completeButton.putObject("action");
            completeAction.put("type", "postback");
            completeAction.put("label", "完成");
            completeAction.put("data", "action=complete_todo&todo_id=" + todo.getId());
            completeAction.put("displayText",
                    "完成「" + (todo.getEntities() != null ? todo.getEntities().getTask() : "") + "」");
            ((ArrayNode) buttonWrapper.putArray("contents")).add(completeButton);
        }

        ObjectNode deleteButton = objectMapper.createObjectNode();
        deleteButton.put("type", "button");
        deleteButton.put("style", "secondary");
        deleteButton.put("height", "sm");
        deleteButton.put("color", "#FF3B30");
        ObjectNode deleteAction = deleteButton.putObject("action");
        deleteAction.put("type", "postback");
        deleteAction.put("label", "刪除");
        deleteAction.put("data", "action=delete_todo&todo_id=" + todo.getId());
        deleteAction.put("displayText", "刪除「" + (todo.getEntities() != null ? todo.getEntities().getTask() : "") + "」");
        ((ArrayNode) buttonWrapper.withArray("contents")).add(deleteButton);

        return buttonWrapper;
    }

    // /**
    // * 動態建立體重紀錄的 Flex Message JSON
    // *
    // * @param latestLog 最新的體重紀錄
    // * @param previousLog 上一次的體重紀錄
    // * @return 替換完數據的 JSON 字串
    // */
    // public String buildWeightLogJson(WeightLog latestLog, WeightLog previousLog)
    // {
    // try {
    // // 1. 讀取 JSON 模板檔案
    // String template;
    // try (InputStream is = new
    // ClassPathResource("flex/weight_log_template.json").getInputStream()) {
    // template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
    // }

    // // --- 2. 準備要替換的數據 ---
    // // 格式化日期和體重數字，讓它們更好看
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M月d日");
    // DecimalFormat df = new DecimalFormat("#.##");

    // String latestWeightStr = df.format(latestLog.getWeight());
    // String latestDateStr = latestLog.getRecordDate().format(formatter);

    // String previousWeightStr = "N/A";
    // String changeIcon = "";
    // String changeValueStr = "-";
    // String changeColor = "#AAAAAA"; // 預設顏色 (灰色)

    // // 如果有上一次的紀錄，才進行比較
    // if (previousLog != null) {
    // previousWeightStr = df.format(previousLog.getWeight());
    // double change = latestLog.getWeight() - previousLog.getWeight();
    // changeValueStr = df.format(Math.abs(change));

    // if (change > 0) {
    // changeIcon = "▲";
    // changeColor = "#EF4444"; // 上升 (紅色)
    // } else if (change < 0) {
    // changeIcon = "▼";
    // changeColor = "#22C55E"; // 下降 (綠色)
    // } else {
    // changeIcon = "─";
    // }
    // }

    // // --- 3. 執行替換 ---
    // String finalJson = template
    // .replace("{{LATEST_WEIGHT}}", latestWeightStr)
    // .replace("{{LATEST_DATE}}", latestDateStr)
    // .replace("{{PREVIOUS_WEIGHT}}", previousWeightStr)
    // .replace("{{CHANGE_ICON}}", changeIcon)
    // .replace("{{CHANGE_VALUE}}", changeValueStr)
    // .replace("{{CHANGE_COLOR}}", changeColor);

    // return finalJson;

    // } catch (Exception e) {
    // log.error("無法建立體重紀錄 Flex Message JSON", e);
    // return null;
    // }
    // }

    // /**
    // * 動態建立體重紀錄的 Carousel Flex Message
    // *
    // * @param weeklyLogs 一週的體重紀錄
    // * @return 一個可以被發送的 FlexMessage 物件
    // */
    // public FlexMessage buildWeeklyWeightLogCarousel(List<WeightLog> weeklyLogs) {
    // if (weeklyLogs == null || weeklyLogs.isEmpty()) {
    // return null;
    // }

    // List<FlexBubble> bubbles = new ArrayList<>();

    // // 遍歷每一天的紀錄，為其建立一個 Bubble 卡片
    // for (int i = 0; i < weeklyLogs.size(); i++) {
    // WeightLog currentLog = weeklyLogs.get(i);
    // // 為了計算趨勢，我們需要前一天的紀錄
    // WeightLog previousLog = (i > 0) ? weeklyLogs.get(i - 1) : null;

    // FlexBubble bubble = createWeightDayBubble(currentLog, previousLog);
    // bubbles.add(bubble);
    // }

    // // 建立 Carousel 容器
    // FlexCarousel carousel = new FlexCarousel(bubbles);

    // return new FlexMessage("您的本週體重紀錄", carousel);
    // }

    // /**
    // * 輔助方法：建立單一一天紀錄的 Bubble
    // */
    // private FlexBubble createWeightDayBubble(WeightLog currentLog, WeightLog
    // previousLog) {
    // // 在這裡，您需要用 Java 的方式，把上面 JSON 結構中的一個 Bubble 建立出來
    // // 這會需要使用 FlexBubble.builder(), FlexBox.builder(), FlexText.builder() 等方法
    // // 這是一個比較進階的用法，需要耐心組合

    // // 為了讓您先看到成果，我們先用一個簡化的方式
    // // 未來您可以把上面 JSON 中的一個 bubble 區塊，用 Java 物件的方式完整重現
    // final FlexText dateText = FlexText.builder()
    // .text(currentLog.getRecordDate().format(DateTimeFormatter.ofPattern("M月d日")))
    // .color("#ffffff")
    // .align(FlexText.Alignment.CENTER)
    // .size(FlexText.ComponentSize.XS)
    // .build();

    // final FlexText dayOfWeekText = FlexText.builder()
    // .text(currentLog.getRecordDate().format(DateTimeFormatter.ofPattern("E",
    // Locale.TAIWAN))) // E 代表 "週X"
    // .color("#ffffff")
    // .align(FlexText.Alignment.CENTER)
    // .size(FlexText.ComponentSize.MD)
    // .weight(FlexText.ComponentWeight.BOLD)
    // .build();

    // final FlexBox header = FlexBox.builder()
    // .layout(FlexLayout.VERTICAL)
    // .paddingAll("12px")
    // .paddingTop("16px")
    // .backgroundColor("#6495ED")
    // .contents(dayOfWeekText, dateText)
    // .build();

    // final FlexText weightText = FlexText.builder()
    // .text(currentLog.getWeight() + " kg")
    // .weight(FlexText.ComponentWeight.BOLD)
    // .size(FlexText.ComponentSize.XL)
    // .align(FlexText.Alignment.CENTER)
    // .build();

    // final FlexBox body = FlexBox.builder()
    // .layout(FlexLayout.VERTICAL)
    // .paddingAll("16px")
    // .contents(weightText)
    // .build();

    // return FlexBubble.builder()
    // .size(FlexBubble.ContainerSize.NANO)
    // .header(header)
    // .body(body)
    // .build();
    // }
}
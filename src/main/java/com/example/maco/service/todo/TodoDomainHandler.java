package com.example.maco.service.todo;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import com.example.maco.domain.model.user.LineMessage;
import com.example.maco.infra.jpa.mapper.TodoMapper;
import com.example.maco.domain.dto.LineMessageDto;
import com.example.maco.domain.dto.TodoResultDto;
import com.example.maco.domain.model.todo.TodoResult;
import com.example.maco.linebot.builder.LineFlexMessageBuilder;
import com.example.maco.linebot.model.PostbackParams;
import com.example.maco.linebot.model.ProcessRequest;
import com.example.maco.linebot.model.ProcessResponse;
import com.example.maco.linebot.service.LineService;
import com.example.maco.linebot.util.NlpClient;
import com.example.maco.service.domain.DomainAction;
import com.example.maco.service.domain.DomainHandler;
import com.example.maco.service.domain.DomainKeys;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class TodoDomainHandler implements DomainHandler {
    private static final Logger log = LoggerFactory.getLogger(TodoDomainHandler.class);

    private final LineService lineService;
    private final TodoService todoService;
    private final ObjectMapper mapper;
    private final List<LineFlexMessageBuilder<?>> builders;
    private Map<String, LineFlexMessageBuilder<?>> registry;

    @PostConstruct
    public void init() {
        registry = builders.stream().collect(Collectors.toMap(LineFlexMessageBuilder::getBuilderKey, b -> b));
    }

    @Override
    public String getDomainKey() {
        return DomainKeys.TODO;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> handleAsync(LineMessage model, String domain) {
        log.info("Processing todo domain={}, userId={}, messageId={}", domain, model.getUserToken(),
                model.getMessageId());
        return NlpClient
                .callNlpApiAsync("/process", new ProcessRequest(domain, model.getMessage()), ProcessResponse.class)
                .timeout(Duration.ofSeconds(10))
                .toFuture()
                .thenAccept(processRes -> processNlpResult(model, domain, processRes))
                .exceptionally(e -> {
                    log.error("NLP /process error, userId={}, messageId={}", model.getUserToken(),
                            model.getMessageId(), e);
                    lineService.sendReply(model.getReplyToken(), "[待辦事項] 系統出現些許錯誤，請稍後再試");
                    return null;
                });
    }

    private void processNlpResult(LineMessage model, String domain, ProcessResponse processRes) {
        log.info("NLP /process response received, userId={}, messageId={}", model.getUserToken(),
                model.getMessageId());

        if (processRes == null || processRes.getResult() == null) {
            log.warn("NLP /process returned null result, userId={}, messageId={}", model.getUserToken(),
                    model.getMessageId());
            lineService.sendReply(model.getReplyToken(), "[待辦事項] 無法處理您的請求，請稍後再試");
            return;
        }

        TodoResultDto todoResultDto = mapper.convertValue(processRes.getResult(), TodoResultDto.class);
        TodoResult todoResult = TodoMapper.toDomain(todoResultDto);
        if (todoResult == null) {
            log.warn("TodoResultDto is null, userId={}, messageId={}", model.getUserToken(), model.getMessageId());
            lineService.sendReply(model.getReplyToken(), "[待辦事項] 無法處理您的請求，請稍後再試");
            return;
        }

        if (!todoResult.isClear()) {
            lineService.sendReply(model.getReplyToken(), "[待辦事項] " + todoResult.getRecommendation());
            return;
        }
        DomainAction action = DomainAction.from(domain, todoResult.getIntent());
        if (action == null) {
            log.warn("Unknown action, userId={}, messageId={}", model.getUserToken(), model.getMessageId());
            lineService.sendReply(model.getReplyToken(), "[待辦事項] 無法處理您的請求，請稍後再試");
            return;
        }

        switch (action) {
            case TODO_ADD -> handleAddTodo(model, todoResult);
            case TODO_QUERY -> handleQueryTodo(model, domain, todoResult);
            default -> lineService.sendReply(model.getReplyToken(), "[待辦事項] " + todoResult.getEntities().getTask());
        }
    }

    private void handleAddTodo(LineMessage model, TodoResult todoResult) {
        String reply;
        try {
            todoService.insertTodo(model.getUserToken(), todoResult);
            reply = todoResult.toUserMessageForAdd();
            lineService.sendReply(model.getReplyToken(), reply);
            log.info("Todo added successfully, userId={}, messageId={}", model.getUserToken(), model.getMessageId());
        } catch (Exception ex) {
            log.error("Failed to add todo, userId={}, messageId={}", model.getUserToken(), model.getMessageId(),
                    ex);
            reply = "新增待辦失敗，請稍後再試";
            lineService.sendReply(model.getReplyToken(), reply);
        }
    }

    private void handleQueryTodo(LineMessage model, String domain, TodoResult todoResult) {
        List<TodoResult> todoResults = todoService.getTodoSummary(model.getUserToken(),
                todoResult.getEntities().getTime().getStartDate(), todoResult.getEntities().getTime().getEndDate());
        if (todoResults.isEmpty()) {
            lineService.sendReply(model.getReplyToken(), "太棒了！您在指定時間範圍內沒有待辦事項。");
            return;
        }
        if (todoResults.size() > 20) {
            lineService.sendReply(model.getReplyToken(),
                    "您有 " + todoResults.size() + " 筆待辦事項，請縮小查詢範圍（目前僅支援查詢 20 筆以內）");
            return;
        }
        buildAndSendFlex(model, domain, todoResults);
    }

    private void buildAndSendFlex(LineMessage model, String domain, List<TodoResult> todoResults) {
        LineFlexMessageBuilder<TodoResult> builder = (LineFlexMessageBuilder<TodoResult>) registry.get(domain);
        if (builder == null) {
            log.error("No Flex builder available for key={} and no default configured", domain);
            lineService.sendReply(model.getReplyToken(), "[系統] 無法產生 Flex 訊息，請稍後再試");
            return;
        }

        String flexMessage = builder.buildObjectListJson(todoResults);
        lineService.sendFlexReplyFromJson(model.getReplyToken(), flexMessage, "Todo List");
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> handlePostback(LineMessageDto dto) {
        String action = dto.getPostbackParams().get(PostbackParams.ACTION);
        String todoIdStr = dto.getPostbackParams().get(PostbackParams.TODO_ID);
        String replyToken = dto.getReplyToken();
        String userToken = dto.getUserToken();
        Long todoId;
        try {
            todoId = Long.parseLong(todoIdStr);
        } catch (NumberFormatException e) {
            log.error("Invalid todo_id in postback params: {}", todoIdStr, e);
            return CompletableFuture.failedFuture(new IllegalArgumentException("invalid todo_id: " + todoIdStr));
        }
        log.info("Handling Postback in TodoDomainHandler: action={}, todoId={}", action, todoId);

        DomainAction domainAction = DomainAction.from(DomainKeys.TODO, action);
        if (domainAction == null) {
            log.warn("Unknown domain action: {}", action);
            return CompletableFuture.failedFuture(new IllegalArgumentException("unknown action: " + action));
        }

        switch (domainAction) {
            case TODO_COMPLETE -> {
                boolean updated = todoService.completeTodoById(userToken, todoId);
                if (updated) {
                    lineService.sendReply(replyToken, "太棒了！該事項已標示為完成！✅");
                } else {
                    log.warn("No todo updated, userId={}, todoId={}", userToken, todoId);
                    lineService.sendReply(replyToken, "哎呀，更新失敗了，請稍後再試。");
                }
            }
            case TODO_DELETE -> {
                boolean deleted = todoService.deleteTodoById(userToken, todoId);
                if (deleted) {
                    lineService.sendReply(replyToken, "該事項已被刪除！🗑️");
                } else {
                    log.warn("No todo deleted, userId={}, todoId={}", userToken, todoId);
                    lineService.sendReply(replyToken, "哎呀，刪除失敗了，請稍後再試。");
                }
            }
            default -> {
                log.warn("Unknown postback action: {}", action);
                lineService.sendReply(replyToken, "未知的操作，請稍後再試。");
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
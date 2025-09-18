package com.example.maco.linebot;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.maco.domain.dto.LineMessageDto;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.ImageMessageContent;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.PostbackEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;

import lombok.RequiredArgsConstructor;

@LineMessageHandler // 告訴 SDK 這是處理 LINE 訊息的類別
@RequiredArgsConstructor
public class LineBotController {

    private static final Logger log = LoggerFactory.getLogger(LineBotController.class);

    private final LineService lineService;

    // 當收到文字訊息時，這個方法會被觸發
    @EventMapping
    public void handleTextMessageEvent(MessageEvent event) {
        if (event.message() instanceof TextMessageContent textMsg) {
            if ("功能說明".equals(textMsg.text())) {
                String message = "Maestro 助理 v1.1 功能說明\n\n"
                        + "我可以幫您：\n"
                        + "✅ 新增待辦事項 (e.g., 明天下午三點去總行開會[時間][地點][事項])\n"
                        + "✅ 查詢待辦事項 (e.g., 查詢本週的待辦事項)\n"
                        + "✅ 完成/刪除事項 (透過查詢列表中的按鈕)\n\n"
                        + "P.S. 目前還不支援直接修改事項內容，建議您可以先刪除再新增喔！";
                lineService.sendReply(event.replyToken(), message);
                return;
            }

            String userId = event.source().userId();
            LineMessageDto dto = new LineMessageDto(
                    userId,
                    textMsg.text(),
                    LocalDateTime.now(),
                    "text",
                    event.replyToken(),
                    textMsg.id(),
                    null);
            lineService.handleTextMessage(dto);
            log.info("收到文字訊息: userId={}, text={}", userId, textMsg.text());
            // lineService.sendReply(dto.getReplyToken(), dto.getMessage());
        } else if (event.message() instanceof ImageMessageContent) {
            lineService.sendReply(event.replyToken(), "收到你的圖片囉！");
            log.info("收到圖片訊息: userId={}", event.source().userId());
        } else {
            log.warn("收到不支援的訊息型態: {}", event.message().getClass().getSimpleName());
        }
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String replyToken = event.replyToken();
        String userId = event.source().userId();

        // 1. 取出我們埋在按鈕裡的 data
        String data = event.postback().data();

        // 2. 解析 data 字串，把它變成一個 Map 方便使用
        Map<String, String> params = Stream.of((data == null ? "" : data).split("&"))
                .map(s -> s.split("=", 2)) // limit=2 避免 value 中有 '=' 時被切掉
                .collect(Collectors.toMap(
                        a -> a[0], // key
                        a -> a.length > 1 ? a[1] : "" // value
                ));

        LineMessageDto lineMessageDto = new LineMessageDto(
                userId,
                null,
                LocalDateTime.now(),
                "text",
                event.replyToken(),
                null,
                params);

        String action = lineMessageDto.getPostbackParams().get("action");
        log.info("收到 Postback 事件: action={}, params={}", action, lineMessageDto.getPostbackParams());
        // 3. 根據 action 的內容，執行對應的商業邏輯
        if ("complete_todo".equals(action)) {
            // 從 params 中取得 todo_id
            long todoId = Long.parseLong(lineMessageDto.getPostbackParams().get("todo_id"));
            log.info("使用者完成待辦事項: todoId={}", todoId);

            // 呼叫您的 Service 去更新資料庫
            boolean success = lineService.completeUserTodo(lineMessageDto);
            if (success) {
                lineService.sendReply(replyToken, "太棒了！該事項已標示為完成！✅");
            } else {
                lineService.sendReply(replyToken, "哎呀，更新失敗了，請稍後再試。");
            }
        } else if ("delete_todo".equals(action)) {
            // 從 params 中取得 todo_id
            long todoId = Long.parseLong(params.get("todo_id"));
            log.info("使用者刪除待辦事項: todoId={}", todoId);

            // 呼叫您的 Service 去更新資料庫
            boolean success = lineService.deleteUserTodo(lineMessageDto);

            if (success) {
                lineService.sendReply(replyToken, "該事項已被刪除！🗑️");
            } else {
                lineService.sendReply(replyToken, "哎呀，刪除失敗了，請稍後再試。");
            }
        }
        // 這裡可以根據不同的 action 做不同的處理
        // 未來還可以增加其他的 action, e.g., "action=modify_todo&todo_id=42"
    }
}
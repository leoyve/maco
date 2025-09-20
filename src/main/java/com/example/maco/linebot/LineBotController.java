package com.example.maco.linebot;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.maco.domain.dto.LineMessageDto;
import com.example.maco.linebot.model.PostbackParams;
import com.example.maco.linebot.service.LineService;
import com.example.maco.linebot.service.RouteService;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.ImageMessageContent;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.PostbackEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;

import lombok.RequiredArgsConstructor;

@LineMessageHandler
@RequiredArgsConstructor
public class LineBotController {

    private static final Logger log = LoggerFactory.getLogger(LineBotController.class);

    private final LineService lineService;
    private final RouteService routeService;

    // 當收到文字訊息時，這個方法會被觸發
    @EventMapping
    public void handleTextMessageEvent(MessageEvent event) {
        if (event.message() instanceof TextMessageContent textMsg) {
            if ("功能說明".equals(textMsg.text()) || "幫助".equals(textMsg.text()) || "help".equalsIgnoreCase(textMsg.text())
                    || "help me".equalsIgnoreCase(textMsg.text())) {
                lineService.sendReply(event.replyToken(), HelpMessageProvider.HELP_MESSAGE);
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
            routeService.handleTextMessage(dto);
            log.info("收到文字訊息: userId={}, text={}", userId, textMsg.text());
        } else if (event.message() instanceof ImageMessageContent) {
            lineService.sendReply(event.replyToken(), "收到你的圖片囉！");
            log.info("收到圖片訊息: userId={}", event.source().userId());
        } else {
            log.warn("收到不支援的訊息型態: {}", event.message().getClass().getSimpleName());
        }
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String userId = event.source().userId();
        String data = event.postback().data();

        // 解析 data 字串，把它變成一個 Map 方便使用
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

        String action = lineMessageDto.getPostbackParams().get(PostbackParams.ACTION);
        log.info("收到 Postback 事件: action={}, params={}", action, lineMessageDto.getPostbackParams());
        routeService.dispatchHandler(lineMessageDto);
    }
}
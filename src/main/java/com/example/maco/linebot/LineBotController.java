package com.example.maco.linebot;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.maco.linebot.model.LineMessageDto;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.ImageMessageContent;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;

@LineMessageHandler // 告訴 SDK 這是處理 LINE 訊息的類別
public class LineBotController {

    @Autowired
    private LineService lineService;

    // 當收到文字訊息時，這個方法會被觸發
    @EventMapping
    public void handleTextMessageEvent(MessageEvent event) {
        if (event.message() instanceof TextMessageContent textMsg) {
            String userId = event.source().userId();
            LineMessageDto dto = new LineMessageDto(
                    userId,
                    textMsg.text(),
                    LocalDateTime.now(),
                    "text",
                    event.replyToken(),
                    textMsg.id());
            lineService.handleTextMessage(dto);
            // lineService.sendReply(dto.getReplyToken(), dto.getMessage());
        } else if (event.message() instanceof ImageMessageContent) {
            lineService.sendReply(event.replyToken(), "收到你的圖片囉！");
        } else {
            System.out.println("收到不支援的訊息型態: " + event.message().getClass().getSimpleName());
        }
    }
}
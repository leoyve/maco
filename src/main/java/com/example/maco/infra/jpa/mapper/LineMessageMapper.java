package com.example.maco.infra.jpa.mapper;

import com.example.maco.domain.dto.LineMessageDto;
import com.example.maco.domain.model.user.LineMessage;
import com.example.maco.infra.jpa.entity.LineMessageEntity;

public class LineMessageMapper {
    public static LineMessageEntity toEntity(LineMessage message) {
        if (message == null) {
            return null;
        }
        LineMessageEntity entity = new LineMessageEntity();
        entity.setUserToken(message.getUserToken());
        entity.setMessage(message.getMessage());
        entity.setReceiveTime(message.getReceiveTime());
        entity.setType(message.getType());
        entity.setReplyToken(message.getReplyToken());
        entity.setMessageId(message.getMessageId());
        return entity;
    }

    public static LineMessage toDomain(LineMessageEntity entity) {
        if (entity == null) {
            return null;
        }
        return new LineMessage(entity.getUserToken(), entity.getMessage(), entity.getReceiveTime(),
                entity.getType(), entity.getReplyToken(), entity.getMessageId());
    }

    public static LineMessage toDomain(LineMessageDto dto) {
        if (dto == null)
            return null;
        return new LineMessage(
                dto.getUserToken(),
                dto.getMessage(),
                dto.getReceiveTime(),
                dto.getType(),
                dto.getReplyToken(),
                dto.getMessageId());
    }
}

package com.example.maco.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/*
    * {
    "intent": "addTodo",
    "entities": {
        "task": "看醫生",
        "time": {
        "timestamp": "2025-08-21 00:00"
        },
        "location": 醫院,
        "status": "TODO"
    },
    "is_clear": true,
    "recommendation": null,
    "domain": "todo"
    }
 */

@Data
public class BaseResultDto {
    private String userToken;
    private String intent;
    private String domain;
    @JsonProperty("is_clear")
    private boolean isClear;
    private String recommendation;
}

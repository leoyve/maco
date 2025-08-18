package com.example.maco.linebot.model;

public class RouterRequest {
    private String text;

    public RouterRequest() {
    }

    public RouterRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

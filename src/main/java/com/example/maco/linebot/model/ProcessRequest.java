package com.example.maco.linebot.model;

public class ProcessRequest {
    private String domain;
    private String text;

    public ProcessRequest() {
    }

    public ProcessRequest(String domain, String text) {
        this.domain = domain;
        this.text = text;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

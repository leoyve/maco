package com.example.maco.linebot.builder;

import java.util.List;

public interface LineFlexMessageBuilder<T> {

    void loadTemplate();

    String buildObjectListJson(List<T> objects);

    String getBuilderKey();
}
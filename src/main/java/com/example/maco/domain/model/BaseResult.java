package com.example.maco.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResult {
    private String intent;
    private String domain;
    private boolean isClear;
    private String recommendation;
}

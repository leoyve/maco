package com.example.maco.service.domain;

public enum DomainAction {
    TODO_COMPLETE("todo", "complete"),
    TODO_DELETE("todo", "delete"),
    TODO_ADD("todo", "addTodo"),
    TODO_QUERY("todo", "queryTodo"),
    WEIGHT_ADD("weight", "addWeight"),
    WEIGHT_QUERY("weight", "queryWeight"),
    WEIGHT_DELETE("weight", "delete"); // 若 weight 也有 delete 行為

    private final String domain;
    private final String action;

    DomainAction(String domain, String action) {
        this.domain = domain;
        this.action = action;
    }

    public String domain() {
        return domain;
    }

    public String action() {
        return action;
    }

    public static DomainAction from(String domain, String action) {
        if (domain == null || action == null)
            return null;
        for (DomainAction a : values()) {
            if (a.domain.equalsIgnoreCase(domain) && a.action.equalsIgnoreCase(action))
                return a;
        }
        return null;
    }
}
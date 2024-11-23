package com.librarysystem.model;

public enum ActionType {
    RESERVE("reserve"),
    BORROW("borrow"),
    RETURN("return");

    private final String value;

    ActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ActionType fromValue(String value) {
        for (ActionType action : ActionType.values()) {
            if (action.value.equalsIgnoreCase(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown ActionType: " + value);
    }
}

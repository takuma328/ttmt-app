package com.example.demo.enums;

public enum Gender {
    MALE("男性"), FEMALE("女性"), OTHER("その他"), NO_ANSWER("回答しない");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
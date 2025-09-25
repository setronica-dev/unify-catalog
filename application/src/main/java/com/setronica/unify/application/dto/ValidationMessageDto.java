package com.setronica.unify.application.dto;

public class ValidationMessageDto {
    private String type;
    private String property;
    private String path;

    public ValidationMessageDto(String type, String property, String path) {
        this.type = type;
        this.property = property;
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public String getProperty() {
        return property;
    }

    public String getPath() {
        return path;
    }
}

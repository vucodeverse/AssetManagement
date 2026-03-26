package edu.fpt.groupfive.common;

public enum UserStatus {
    ACTIVE("Hoạt động"),
    INACTIVE("Không hoạt động");

    private final String description;
    UserStatus(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}

package edu.fpt.groupfive.common;

public enum Priority {
    LOW("Thấp"), MEDIUM("Trung Bình"), HIGH("Cao"), CRITICAL("Quan trọng");

    private String description;

    Priority(String s) {
        this.description = s;
    }

    public String getDescription() {
        return description;
    }
}

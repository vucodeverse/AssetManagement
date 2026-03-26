package edu.fpt.groupfive.common;

public enum     Priority {
    LOW("THẤP"), MEDIUM("TRUNG BÌNH"), HIGH("CAO"), CRITICAL("QUAN TRỌNG");

    private String description;

    Priority(String s) {
        this.description = s;
    }

    public String getDescription() {
        return description;
    }
}

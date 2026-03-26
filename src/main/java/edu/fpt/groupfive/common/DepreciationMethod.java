package edu.fpt.groupfive.common;

public enum DepreciationMethod {
    STRAIGHT_LINE("Khấu hao đường thẳng"),
    DECLINING_BALANCE("Khấu hao giảm dần");

    private final String description;

    DepreciationMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
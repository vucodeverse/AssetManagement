package edu.fpt.groupfive.common;

public enum OrderStatus {
    PENDING("Đang chờ"),
    APPROVED("Đã duyệt"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã bị hủy");

    private String description;

    OrderStatus(String s) {
        this.description = s;
    }

    public String getDescription() {
        return description;
    }
}

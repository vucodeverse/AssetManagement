package edu.fpt.groupfive.common;

public enum Request {
    DRAFT("Tạm thời"),
    PENDING("Đang chờ"),
    APPROVED("Đã chấp nhận"),
    ORDERED("Đã đặt hàng"),
    REJECTED("Từ chối"),
    DELETED("Đã bị xóa");

    private String description;

    Request(String s) {
        this.description = s;
    }

    public String getDescription() {
        return description;
    }
}

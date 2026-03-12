package edu.fpt.groupfive.common;

public enum Request {
    DRAFT("Tạm thời"),
    PENDING("Đang chờ"),
    APPROVED("Đã chấp nhận"),
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

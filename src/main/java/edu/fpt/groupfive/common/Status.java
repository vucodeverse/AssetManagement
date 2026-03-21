package edu.fpt.groupfive.common;

public enum Status {
    DRAFT("Tạm thời"),
    PENDING("Đang chờ"),
    APPROVED("Đã chấp nhận"),
    REJECTED("Từ chối"),
    DELETED("Đã bị xóa");

    private String description;

    Status(String s) {
        this.description = s;
    }

    public String getDescription() {
        return description;
    }



}

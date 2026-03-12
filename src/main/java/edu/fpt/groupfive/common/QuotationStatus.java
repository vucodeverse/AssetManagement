package edu.fpt.groupfive.common;

public enum QuotationStatus {
    DRAFT("Tạm thời"),
    PENDING("Đang chờ"),
    APPROVED("Đã chấp nhận"),
    REJECTED("Từ chối"),
    DELETED("Đã bị xóa");

    private String description;

    QuotationStatus(String s) {
        this.description = s;
    }

    public String getDescription() {
        return description;
    }


}

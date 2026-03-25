package edu.fpt.groupfive.common;

public enum PurchaseProcessStatus {
    DRAFT("Tạm thời"),
    PENDING("Đang chờ"),
    APPROVED("Đã chấp nhận"),
    ORDERED("Đã đặt hàng toàn bộ"),
    REJECTED("Từ chối"),
    DELETED("Đã bị xóa"),
    COMPLETED("Đã hoàn thành"),
    PARTIALLY_RECEIVED("Nhập kho một phần"),
    CANCELLED("Đã bị hủy");

    private String description;

    PurchaseProcessStatus(String s) {
        this.description = s;
    }

    public String getDescription() {
        return description;
    }
}
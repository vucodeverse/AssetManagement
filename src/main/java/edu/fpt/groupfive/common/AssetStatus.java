package edu.fpt.groupfive.common;

public enum AssetStatus {
    AVAILABLE("Sẵn sàng"),
//    ALLOCATED("Đang chờ cấp phát"),
    ASSIGNED("Đã bàn giao"),
    UNDER_MAINTENANCE("Đang sửa chữa"),
    DISPOSED("Thanh lý"),
    DELETED("Đã xóa");
    private final String description;

    AssetStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

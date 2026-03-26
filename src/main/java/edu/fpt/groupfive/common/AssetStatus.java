package edu.fpt.groupfive.common;

public enum AssetStatus {
    NEW("Mới tạo"),
    AVAILABLE("Sẵn sàng"),
    ALLOCATED("Đang chờ cấp phát"),
    ASSIGNED("Đã bàn giao"),
    UNDER_MAINTENANCE("Đang sửa chữa"),
    DISPOSED("Thanh lý");

    private final String description;

    AssetStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

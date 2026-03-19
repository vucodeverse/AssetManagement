package edu.fpt.groupfive.common;

public enum AssetStatus {
    NEW, // Vừa tạo, chưa được kho tiếp nhận

    AVAILABLE, // Đã ở kho và sẵn sàng sử dụng

    ASSIGNED, // Đã bàn giao cho người dùng

    UNDER_MAINTENANCE, // Đang sửa chữa

    DISPOSED// Thanh lý / ngừng sử dụng
}

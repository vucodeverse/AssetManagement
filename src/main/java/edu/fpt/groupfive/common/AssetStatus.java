package edu.fpt.groupfive.common;

public enum AssetStatus {
    NEW, // Vừa tạo, chưa được kho tiếp nhận

    AVAILABLE, // Đã ở kho và sẵn sàng sử dụng

    ALLOCATED, // Đã được gán cho lệnh cấp phát, đang chờ bàn giao

    ASSIGNED, // Đã bàn giao cho người dùng

    UNDER_MAINTENANCE, // Đang sửa chữa

    DISPOSED// Thanh lý / ngừng sử dụng
}

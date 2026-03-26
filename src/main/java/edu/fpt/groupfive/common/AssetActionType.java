package edu.fpt.groupfive.common;

public enum AssetActionType {

    CREATE("Tạo mới"),
    ALLOCATE("Cấp phát"),
    TRANSFER("Điều chuyển"),
    RETURN("Trả lại"),
    STATUS_CHANGE("Thay đổi trạng thái"),
    DISPOSE("Thanh lý");

    private final String description;

    AssetActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}

package edu.fpt.groupfive.common;

public enum Role {
    ADMIN("Quản trị viên"),
    PURCHASE_STAFF("Nhân viên mua sắm"),
    ASSET_MANAGER("Quản lý tài sản"),
    DEPARTMENT_MANAGER("Trưởng phòng ban"),
    WAREHOUSE_STAFF("Nhân viên kho"),
    DIRECTOR("Giám đốc");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

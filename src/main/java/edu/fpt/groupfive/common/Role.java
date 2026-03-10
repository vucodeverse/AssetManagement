package edu.fpt.groupfive.common;

import java.util.List;

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

    public static List<Role> getRoles() {
        return List.of(
                DIRECTOR,
                DEPARTMENT_MANAGER,
                ASSET_MANAGER,
                PURCHASE_STAFF,
                WAREHOUSE_STAFF
        );
    }
}

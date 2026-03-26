package edu.fpt.groupfive.common;

public enum AssetTypeClass {
    FIXED_ASSET("Tài sản cố định"),
    TOOL("Công cụ dụng cụ"),
    EQUIPMENT("Thiết bị"),
    CONSUMABLE("Vật tư tiêu hao"),
    HARDWARE("Phần cứng"),
    SOFTWARE("Phần mềm"),
    ELECTRONICS("Điện tử"),
    FURNITURE("Nội thất"),
    IT_ASSET("Tài sản CNTT"),
    OFFICE_ASSET("Tài sản văn phòng");

    private final String description;

    AssetTypeClass(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
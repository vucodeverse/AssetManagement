package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WarehouseStatus {
    ACTIVE("Đang hoạt động"),
    INACTIVE("Dừng hoạt động");
    private String displayName;
}

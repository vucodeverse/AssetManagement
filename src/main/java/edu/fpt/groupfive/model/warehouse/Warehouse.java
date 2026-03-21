package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {
    private Integer warehouseId;
    private String name;
    private String address;
    private Integer managerUserId;
    private String status;
}

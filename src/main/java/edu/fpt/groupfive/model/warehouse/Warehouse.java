package edu.fpt.groupfive.model.warehouse;

import edu.fpt.groupfive.model.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Warehouse extends AbstractEntity<Integer> {
    private String name;
    private String address;
    private WarehouseStatus status=WarehouseStatus.INACTIVE;
    private Integer managerId;


    public boolean isActive() {
        return status == WarehouseStatus.ACTIVE;
    }

    public void active() {
        status = WarehouseStatus.ACTIVE;
    }

    public void deactive() {
        status = WarehouseStatus.INACTIVE;
    }

}

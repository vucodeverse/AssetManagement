package edu.fpt.groupfive.model.warehouse;

import edu.fpt.groupfive.model.Asset;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Zone {
    private Integer id;
    private Integer warehouseId;
    private String name;
    private Integer assignedAssetTypeId;
    private Integer maxCapacity;
    private Integer currentCapacity;
    private ActiveStatus status; // ACTIVE, INACTIVE

    public boolean isActive() {
        return status == ActiveStatus.ACTIVE;
    }
    public void active(){
        this.status = ActiveStatus.ACTIVE;
    }

    public void deactive(){
        this.status = ActiveStatus.INACTIVE;
    }
}

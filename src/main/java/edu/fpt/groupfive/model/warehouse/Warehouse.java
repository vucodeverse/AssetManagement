package edu.fpt.groupfive.model.warehouse;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Warehouse {
    private Integer id;
    private String name;
    private String address;
    private Integer managerUserId;
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

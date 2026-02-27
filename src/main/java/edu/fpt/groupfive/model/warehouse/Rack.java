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
public class Rack extends AbstractEntity<Integer> {
    private Integer warehouseId;
    private String name;
    private String description;
    private String status;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}

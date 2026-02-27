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
public class Shelf extends AbstractEntity<Integer> {
    private String name;
    private Integer currentCapacity;
    private Integer maxCapacity;
    private String description;
    private Integer rackId;
    private String status;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isFull() {
        return currentCapacity != null && maxCapacity != null && currentCapacity >= maxCapacity;
    }
}

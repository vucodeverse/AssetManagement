package edu.fpt.groupfive.dto.warehouse.response;

import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WarehouseResponseDTO {
        private Integer id;
        private String name;
        private String address;
        private Integer managerUserId;
        private ActiveStatus status;

        public boolean isActive() {
                return status == ActiveStatus.ACTIVE;
        }
}

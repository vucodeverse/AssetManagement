package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class PODetailResponseDTO {
    private Integer purchaseOrderId;
    private String supplierName;
    private String status;
    private List<POItemDetailDTO> items;
}

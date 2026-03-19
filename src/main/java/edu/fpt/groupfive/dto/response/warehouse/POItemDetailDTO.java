package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class POItemDetailDTO {
    private Integer assetTypeId;
    private String assetTypeName;
    private Integer quantity; // Số lượng đặt mua
    private Integer receivedQuantity; // Số lượng đã nhận (nếu có)
}

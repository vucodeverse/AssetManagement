package edu.fpt.groupfive.dto.request.warehouse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InboundPOReceiveRequestDTO {

    @NotNull(message = "Mã đơn mua không được để trống")
    private Integer purchaseOrderId;

    private List<POItemReceiveRequestDTO> items;
}

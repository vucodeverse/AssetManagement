package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderCreateRequest {
    private Integer quotationId;

    @NotNull(message = "Nhà cung cấp không được để trống")
    private Integer supplierId;

    @Size(max = 255, message = "Không được quá 255 kí tự")
    @Pattern(regexp = "^[a-zA-Z0-9 ]*$", message = "Không được chứa ký tự đặc biệt")
    private String orderNote;
    private BigDecimal totalAmount;

    @NotNull(message = "Tên kho không được để trống")
    private String warehouseName;

    @Builder.Default
    private List<PurchaseOrderDetailCreateRequest> purchaseOrderDetailCreateRequests = new ArrayList<>();
}

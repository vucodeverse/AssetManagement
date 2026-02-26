package edu.fpt.groupfive.dto.request;

import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationCreateRequest {
    private Integer quotationId; //TODO: ????

    @NotNull(message = "Purchase id không được để trống")
    private Integer purchaseRequestId;
    private String quotationNote;

    @NotNull(message = "Vui lòng chọn nhà cung cấp")
    private Integer supplierId;

    @Builder.Default
    private List<QuotationCreateDetailRequest> quotationCreateDetailRequestList = new ArrayList<>();
}

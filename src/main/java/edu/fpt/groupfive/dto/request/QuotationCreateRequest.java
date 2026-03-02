package edu.fpt.groupfive.dto.request;

import jakarta.validation.Valid;
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
    private Integer quotationId; // TODO: ????

    @NotNull(message = "Purchase id không được để trống")
    private Integer purchaseId;
    private String quotationNote;

    @NotNull(message = "Vui lòng chọn nhà cung cấp")
    private Integer supplierId;

    @Builder.Default
    @Valid
    private List<QuotationDetailCreateRequest> quotationDetailCreateRequests = new ArrayList<>();
}

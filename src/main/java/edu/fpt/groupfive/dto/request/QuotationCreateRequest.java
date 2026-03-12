package edu.fpt.groupfive.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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
    private Integer quotationId;

    @NotNull(message = "Purchase id không được để trống")
    private Integer purchaseId;

    @Pattern(regexp = "^[a-zA-Z0-9 ]*$", message = "Không được chứa ký tự đặc biệt")
    private String quotationNote;

    @NotNull(message = "Vui lòng chọn nhà cung cấp")
    private Integer supplierId;

    @Builder.Default
    @Valid
    private List<QuotationDetailCreateRequest> quotationDetailCreateRequests = new ArrayList<>();
}

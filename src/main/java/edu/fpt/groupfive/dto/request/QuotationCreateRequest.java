package edu.fpt.groupfive.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @Pattern(regexp = "^[a-zA-Z0-9À-ỹ ]*$", message = "Không được chứa ký tự đặc biệt")
    @Size(max = 255, message = "Không được điền quá 255 kí tự")
    private String quotationNote;

    @NotNull(message = "Vui lòng chọn nhà cung cấp")
    private Integer supplierId;

    @Builder.Default
    @Valid
    private List<QuotationDetailCreateRequest> quotationDetailCreateRequests = new ArrayList<>();
}

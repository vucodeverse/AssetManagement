package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.model.ReturnRequestDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ReturnRequestCreateRequest {
    private Integer requestId;
    private Integer requesterId;
    private Integer requestedDepartmentId;
    private String status;
    @NotBlank(message = "Lí do không để trống!")
    @Pattern(regexp = "^[\\p{L}a-zA-Z0-9 .,_-]+$", message = "Lí do trả không được chứa kí tự đặc biệt")
    private String requestReason;
    @NotEmpty(message = "Danh sách chi tiết không được để trống!")
    @Valid
    private List<ReturnRequestDetailRequest> details;
}

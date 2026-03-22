package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.model.ReturnRequestDetail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String requestReason;
    @NotNull(message = "Danh sách chi tiết không được để trống!")
    private List<ReturnRequestDetailRequest> details;
}

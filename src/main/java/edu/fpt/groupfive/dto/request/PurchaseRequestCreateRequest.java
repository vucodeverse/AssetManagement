package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestCreateRequest {
    private Integer purchaseId;

    private String purchaseNote;

    @NotNull(message = "Ngày cần cấp Không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Thời gian nhập vào phải lớn hơn thời gian hiện tại")
    private LocalDate neededByDate;

    @NotBlank(message = "Lý do Không được để trống")
    private String reason;

    @NotNull(message = "Độ ưu tiên Không được để trống")
    private Priority priority;

    @Valid
    private List<PurchaseRequestDetailCreateRequest> purchaseRequestDetailCreateRequests = new ArrayList<>();
}

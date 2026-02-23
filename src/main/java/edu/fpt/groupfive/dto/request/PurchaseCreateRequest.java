package edu.fpt.groupfive.dto.request;


import edu.fpt.groupfive.common.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseCreateRequest {

    @NotBlank(message = "Note Không được để trống")
    private String note;

    @NotNull(message = "Needed By Date Không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Thời gian nhập vào phải lớn hơn thời gian hiện tại")
    private LocalDate neededByDate;

    @NotBlank(message = "Reason Không được để trống")
    private String reason;

    @NotNull(message = "Priority Không được để trống")
    private String priority;

    private List<PurchaseDetailCreateRequest> purchaseDetailCreateRequests = new ArrayList<>();
}

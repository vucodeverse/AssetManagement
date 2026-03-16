package edu.fpt.groupfive.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AllocationRequestCreateRequest {
    private Integer requestId;
    private Integer requesterId;
    private Integer requestedDepartmentId;

    @NotBlank(message = "Lí do xin cấp chưa nhập")
    private String requestReason;

    @NotBlank(message = "Độ ưu tiên chưa được chọn")
    private String priority;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Thời điểm cần nhận phải từ hôm nay trở đi")
    private LocalDate neededByDate;

    private String status;

    @NotEmpty(message = "Danh sách tài sản không được rỗng")
    @Valid
    private List<AllocationRequestDetailRequest> details;
}

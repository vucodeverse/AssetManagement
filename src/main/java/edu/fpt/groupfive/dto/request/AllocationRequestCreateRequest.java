package edu.fpt.groupfive.dto.request;

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
    private String requestReason;
    private String priority;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate neededByDate;
    private String status;
    private List<AllocationRequestDetailRequest> details;
}

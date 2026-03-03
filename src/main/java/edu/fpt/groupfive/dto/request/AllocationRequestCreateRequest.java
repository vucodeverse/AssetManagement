package edu.fpt.groupfive.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AllocationRequestCreateRequest {
    private Integer requesterId;
    private Integer requestedDepartmentId;
    private String requestReason;
    private String priority;
    private LocalDate neededByDate;
    private List<AllocationRequestDetailRequest> details;
}

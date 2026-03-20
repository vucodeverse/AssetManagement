package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AllocationRequestResponse {
    private Integer requestId;

    private String status;

    private String priority;

    private LocalDate neededByDate;

    private LocalDateTime createdAt;

    private String requesterName;
}

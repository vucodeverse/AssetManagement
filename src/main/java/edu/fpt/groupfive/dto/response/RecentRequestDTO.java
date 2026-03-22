package edu.fpt.groupfive.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentRequestDTO {
    private Integer requestId;
    private String code;
    private String date;
    private String reason;
    private String statusText;
    private String statusClass;
}
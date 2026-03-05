package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditUpdateRequest {
    private Integer id;
    private String status;
    private String note;
}

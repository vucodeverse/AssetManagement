package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.Request;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseResponse {
    private Integer id;
    private Request status;
    private Integer createdByUser;
    private Date neededByDate;
    private String priority;
    private LocalDate createdAt;
}

package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class Purchase extends AbstractEntity<Integer>{

    private Request status;
    private String purchaseNote;
    private String rejectReason;
    private Integer createdByUser;
    private LocalDate neededByDate;
    private String reason;
    private Priority priority;
    private Integer approvedByDirector;
    private LocalDateTime approvedAt;
    private List<PurchaseDetail> purchaseDetails;
    private List<Quotation> quotations;
}

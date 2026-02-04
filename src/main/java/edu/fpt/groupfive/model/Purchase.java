package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.PurchaseRequestStatus;
import edu.fpt.groupfive.common.Request;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class Purchase extends AbstractEntity<Integer>{

    private Request status;
    private String note;
    private String rejectReason;
    private Integer createdByUser;
    private Date neededByDate;
    private String reason;
    private String priority;
    private Integer approvedByDirector;
    private LocalDateTime approvedAt;
    private Integer purchaseStaffId;
    private List<PurchaseDetail> purchaseDetails;
}

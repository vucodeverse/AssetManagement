package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.PurchaseRequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class PurchaseRequest extends AbstractEntity<Integer>{

    private PurchaseRequestStatus purchaseRequestStatus;
    private String note;
    private String rejectReason;
    private Integer createdByUser;
    private Integer requestFromDepartment;
    private Date neededByDate;
    private String priority;
    private Integer approvedByDirector;
    private List<PurchaseRequestDetail> purchaseRequestDetailSet;
}

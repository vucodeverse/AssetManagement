package edu.fpt.groupfive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransferRequest {
    private int transferId;
    private Integer allocationRequestId;
    private Integer fromDepartmentId;
    private Integer toDepartmentId;
    private Integer assetManagerId;
    private LocalDateTime transferDate;
    private String reason;
    private String status;// PENDING, SENDER_CONFIRMED, WAREHOUSE_CONFIRMED, RECEIVER_CONFIRMED, CANCELLED

    private Integer senderConfirmedBy;
    private LocalDateTime senderConfirmedAt;

    private Integer whConfirmedBy;
    private LocalDateTime whConfirmedAt;

    private Integer receiverConfirmedBy;
    private LocalDateTime receiverConfirmedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
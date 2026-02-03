package edu.fpt.groupfive.model;

public abstract class AbstractRequest <T> extends AbstractEntity<T>{
    private String status;
    private Integer createdByUserId;
    private Integer departmentId;
    private String note;
    private String rejectReason;
}

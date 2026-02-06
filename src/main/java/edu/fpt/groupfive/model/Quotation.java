package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.QuotationStatus;
import org.springframework.security.core.userdetails.User;

public class Quotation extends AbstractEntity<Integer>{

    private QuotationStatus status;
    private String note;
    private User user;
    private Supplier supplier;
    private Purchase purchase;
}

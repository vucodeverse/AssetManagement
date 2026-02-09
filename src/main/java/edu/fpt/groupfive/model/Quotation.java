package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.QuotationStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class Quotation extends AbstractEntity<Integer>{

    private QuotationStatus status;
    private String quotationDetailNote;
    private LocalDate quoationDate;
    private Supplier supplier;
    private Purchase purchase;
    private BigDecimal totalAmount;
}

package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import jakarta.validation.constraints.Future;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PurchaseRequestSearchCriteria {
    private Request status;
    private Priority priority;
    private String keyword;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Ngày tháng nhập vào không được ở trong quá khứ")
    private LocalDate from;

    @Future(message = "Ngày tháng nhập vào không được ở trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;
}

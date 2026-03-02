package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PurchaseSearchAndFilter {
    private Request status;
    private Priority priority;
    private String keyword;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;
}

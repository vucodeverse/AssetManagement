package edu.fpt.groupfive.dto.request.search;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransferSearchCriteria {
    private String status;          // trạng thái lọc
    private LocalDate fromDate;     // từ ngày
    private LocalDate toDate;       // đến ngày
}
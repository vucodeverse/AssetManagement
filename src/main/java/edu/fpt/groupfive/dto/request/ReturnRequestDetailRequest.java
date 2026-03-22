package edu.fpt.groupfive.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ReturnRequestDetailRequest {
    private Integer assetId;
    private String note;
}

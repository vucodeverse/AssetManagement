package edu.fpt.groupfive.dto.request.search;

import edu.fpt.groupfive.common.AssetStatus;
import lombok.*;

import java.time.LocalDate;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssetSearchCriteria {

    private String keyword;
    private AssetStatus status;
    private LocalDate acquisitionFrom;
    private LocalDate acquisitionTo;
    private String direction;
    private Integer departmentId;

}
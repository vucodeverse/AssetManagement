package edu.fpt.groupfive.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetDistributionDTO {
    private String typeName;
    private Long count;
    private Double percentage;
}
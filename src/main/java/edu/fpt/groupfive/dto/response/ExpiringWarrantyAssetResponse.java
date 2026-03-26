package edu.fpt.groupfive.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ExpiringWarrantyAssetResponse {

    private  Integer assetId;
    private String assetName;
    private LocalDate warrantyEndDate;
    private int daysRemaining;
}

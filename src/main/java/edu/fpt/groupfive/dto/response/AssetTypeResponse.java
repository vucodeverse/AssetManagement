package edu.fpt.groupfive.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetTypeResponse {

    private Integer typeId;
    private String typeName;
}

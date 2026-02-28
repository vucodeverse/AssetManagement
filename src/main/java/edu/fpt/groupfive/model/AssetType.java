package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.AssetTypeClass;
import edu.fpt.groupfive.common.DepreciationMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssetType {
    private Integer typeId;
    private String typeName;
    private String description;
    private AssetTypeClass typeClass;
    private  String status;
    private DepreciationMethod defaultDepreciationMethod;
    private  Integer defaultUsefulLifeMonths;
    private  String specification;
    private String model;
    private  Integer categoryId;

    private String categoryName;
}

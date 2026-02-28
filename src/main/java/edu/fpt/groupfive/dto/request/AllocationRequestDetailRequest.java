    package edu.fpt.groupfive.dto.request;

    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public class AllocationRequestDetailRequest {
        private Integer assetTypeId;
        private Integer requestedQuantity;
        private String note;
    }

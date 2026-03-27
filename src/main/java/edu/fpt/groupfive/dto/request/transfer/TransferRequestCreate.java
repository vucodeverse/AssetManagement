    package edu.fpt.groupfive.dto.request.transfer;

    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    import java.util.List;
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public class TransferRequestCreate {
        private Integer fromDepartmentId;
        private Integer toDepartmentId;
        private Integer assetManagerId;
        private String reason;
        private List<Integer> assetIds;

        private Integer allocationRequestId;
    }

package edu.fpt.groupfive.dto.response.dashboardadmin;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    private long totalAssets;
    private long totalUsers;
    private long totalDepartments;

    private long newPurchaseRequests;
    private long newAllocationRequests;
    private long newReturnRequests;
    private long newTransferRequests;

    private List<UserDTO> users;
    private List<DepartmentDTO> departments;
}

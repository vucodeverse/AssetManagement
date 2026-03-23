package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.dto.response.UserResponse;
import edu.fpt.groupfive.dto.response.dashboardadmin.AdminDashboardDTO;
import edu.fpt.groupfive.dto.response.dashboardadmin.DepartmentDTO;
import edu.fpt.groupfive.dto.response.dashboardadmin.UserDTO;
import edu.fpt.groupfive.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardAdminServiceImpl implements DashboardAdminService {

    private final AssetService assetService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final PurchaseService purchaseService;
    private final AllocationRequestService allocationRequestService;
    private final ReturnRequestService returnRequestService;

    private List<UserDTO> getUsers() {

        List<UserDTO> list = new ArrayList<>();

        List<UserResponse> users = userService
                .searchUsers(1, 5, null, null, null, "");

        for (UserResponse u : users) {

            String role = "N/A";

            if (u.getRole() != null) {
                role = u.getRole().getDisplayName();
            }

            list.add(new UserDTO(
                    "#" + u.getUserId(),
                    u.getFullName(),
                    u.getEmail(),
                    role));
        }

        return list;
    }

    List<DepartmentDTO> getDepartments() {
        List<DepartmentDTO> list = new ArrayList<>();

        List<DepartmentResponse> departments = departmentService.getDepartmentsPaged(1, 5);

        for (DepartmentResponse d : departments) {
            list.add(new DepartmentDTO(
               "#" + d.getDepartmentId(),
               d.getDepartmentName(),
               departmentService.countStaffInDepartment(d.getDepartmentId())
            ));
        }

        return list;
    }


    @Override
    public AdminDashboardDTO getAdminDashboardData() {
        // Tổng số asset trong công ty
        int totalAssets = assetService.getAll().size();
        // Tổng số department trong công ty
        int totalDepartments = departmentService.countDepartments();
        // Tổng số user trong công ty
        int totalUsers = userService.getUserIdToUsernameMap().size();
        // Tổng số yêu cầu cấp
        int totalAllocationRequest = allocationRequestService.getTotalPending();
        // Tổng số yêu cầu trả
        int totalReturnRequest = returnRequestService.getTotalPending();
        // Tổng số yêu cầu mua sắm
        int totalPurchaseRequest = purchaseService.getTotalRequest();
        // Lấy danh sách người dùng
        List<UserDTO> users = getUsers();
        // Lấy danh sách phòng ban
        List<DepartmentDTO> departments = getDepartments();


        return AdminDashboardDTO.builder()
                .totalAssets(totalAssets)
                .totalDepartments(totalDepartments)
                .totalUsers(totalUsers)
                .newAllocationRequests(totalAllocationRequest)
                .newReturnRequests(totalReturnRequest)
                .newPurchaseRequests(totalPurchaseRequest)
                .users(users)
                .departments(departments)
                .build();
    }
}

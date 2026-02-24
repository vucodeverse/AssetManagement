    package edu.fpt.groupfive.service;

    import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
    import edu.fpt.groupfive.dto.request.DepartmentUpdateRequest;
    import edu.fpt.groupfive.dto.response.DepartmentResponse;
    import edu.fpt.groupfive.model.Department;

    import java.util.List;

    public interface DepartmentService {
        void createDepartment(DepartmentCreateRequest request);
        void updateDepartment(DepartmentUpdateRequest request);
        void removeDepartment(Integer id);
        List<DepartmentResponse> getAllDepartments();
        DepartmentResponse getDepartById(Integer id);
        List<DepartmentResponse> searchDepartments(String keyword);
        List<DepartmentResponse> getDepartmentsPaged(int page, int size);
        int countStaffInDepartment(Integer departmentId);
        int countDepartments();
    }

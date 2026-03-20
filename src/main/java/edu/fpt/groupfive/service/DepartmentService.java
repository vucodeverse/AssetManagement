    package edu.fpt.groupfive.service;

    import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
    import edu.fpt.groupfive.dto.request.DepartmentUpdateRequest;
    import edu.fpt.groupfive.dto.response.DepartmentResponse;

    import java.util.List;

    public interface DepartmentService {
        void createDepartment(DepartmentCreateRequest request);
        void updateDepartment(DepartmentUpdateRequest request);
        List<DepartmentResponse> findAll();
    }

    package edu.fpt.groupfive.service;

    import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
    import edu.fpt.groupfive.dto.request.DepartmentUpdateRequest;

    public interface DepartmentService {
        void createDepartment(DepartmentCreateRequest request);
        void updateDepartment(DepartmentUpdateRequest request);
    }

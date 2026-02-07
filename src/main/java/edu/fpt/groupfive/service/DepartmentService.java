    package edu.fpt.groupfive.service;

    import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
    import edu.fpt.groupfive.dto.request.DepartmentUpdateRequest;
    import edu.fpt.groupfive.model.Department;

    import java.util.List;

    public interface DepartmentService {
        void createDepartment(DepartmentCreateRequest request);
        void updateDepartment(DepartmentUpdateRequest request);
        void removeDepartment(Integer id);
        List<Department> getAllDepartments();
        Department getDepartById(Integer id);
    }

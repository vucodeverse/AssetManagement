package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentDAO {
    void insert(Department department);

    void update(Department department);

    void delete(Integer departmentId);

    Optional<Department> findById(Integer departmentId);

    List<Department> findAll();

    boolean existsByName(String departmentName, Integer departId);

    List<Department> searchByName(String keyword);

    List<Department> findAllPaged(int page, int size);

    int countDepartments();

    void updateManager(Integer departmentId, Integer userId);

}

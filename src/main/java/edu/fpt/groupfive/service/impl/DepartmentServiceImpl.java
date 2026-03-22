package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
import edu.fpt.groupfive.dto.request.DepartmentUpdateRequest;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.mapper.DepartmentMapper;
import edu.fpt.groupfive.model.Department;
import edu.fpt.groupfive.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    public final DepartmentDAO departmentDAO;
    public final DepartmentMapper departmentMapper;
    public final UserDAO userDAO;

    @Override
    public void createDepartment(DepartmentCreateRequest request) {
        if (departmentDAO.existsByName(request.getDepartmentName())) {
            throw new IllegalArgumentException("Department already exists");
        }

        Department d = departmentMapper.toDepartment(request);
        d.setStatus("ACTIVE");
        d.setCreatedDate(LocalDateTime.now());

        departmentDAO.insert(d);

    }

    @Override
    public void updateDepartment(DepartmentUpdateRequest request) {

        Department existing = departmentDAO.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Not found"));

        departmentMapper.updateDepartFromRequest(request, existing);
        existing.setUpdatedDate(LocalDateTime.now());

        departmentDAO.update(existing);
    }

    @Override
    public void removeDepartment(Integer id) {
        departmentDAO.delete(id);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentMapper.toResponseList(departmentDAO.findAll());
    }

    @Override
    public DepartmentResponse getDepartById(Integer id) {
        Department department = departmentDAO.findById(id)
                        .orElseThrow(() -> new RuntimeException("Not found"));
        return departmentMapper.toDepartmentResponse(department);
    }

    @Override
    public List<DepartmentResponse> searchDepartments(String keyword) {
        return departmentMapper.toResponseList(
                departmentDAO.searchByName(keyword)
        );
    }

    @Override
    public List<DepartmentResponse> getDepartmentsPaged(int page, int size) {
        return departmentMapper.toResponseList(
                departmentDAO.findAllPaged(page, size)
        );
    }

    @Override
    public int countDepartments() {
        return departmentDAO.countDepartments();
    }

    @Override
    public boolean existsDepartmentName(String departmentName, Integer departId) {
        return departmentDAO.existsByName(departmentName, departId);
    }

    @Override
    public List<DepartmentResponse> findAll() {

        return departmentDAO.findAll()
                .stream()
                .map(dept -> new DepartmentResponse(
                        dept.getDepartmentId(),
                        dept.getDepartmentName()
                ))
                .toList();
    }

    @Override
    public int countStaffInDepartment(Integer departmentId) {
        return userDAO.countUsersInDepartment(departmentId);
    }

}

package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
import edu.fpt.groupfive.dto.request.DepartmentUpdateRequest;
import edu.fpt.groupfive.model.Department;
import edu.fpt.groupfive.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    public final DepartmentDAO departmentDAO;

    @Override
    public void createDepartment(DepartmentCreateRequest request) {
        if (departmentDAO.existsByName(request.getDepartmentName())) {
            throw new IllegalArgumentException("Department already exists");
        }

        Department d = new Department();
        d.setDepartmentName(request.getDepartmentName());
        d.setStatus("ACTIVE");
        d.setCreatedDate(LocalDateTime.now());

        departmentDAO.insert(d);

    }

    @Override
    public void updateDepartment(DepartmentUpdateRequest request) {

    }
}

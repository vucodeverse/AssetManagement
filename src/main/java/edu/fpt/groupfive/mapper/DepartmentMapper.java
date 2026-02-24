package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
import edu.fpt.groupfive.dto.request.DepartmentUpdateRequest;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    Department toDepartment(DepartmentCreateRequest request);

    void updateDepartFromRequest(DepartmentUpdateRequest request,
                                 @MappingTarget Department department);

    DepartmentResponse toDepartmentResponse(Department department);

    List<DepartmentResponse> toResponseList(List<Department> departments);
}

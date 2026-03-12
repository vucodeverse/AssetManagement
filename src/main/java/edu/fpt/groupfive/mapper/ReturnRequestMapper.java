package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.AllocationRequestDetailRequest;
import edu.fpt.groupfive.dto.request.ReturnRequestCreateRequest;
import edu.fpt.groupfive.dto.request.ReturnRequestDetailRequest;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.dto.response.ReturnRequestRespnse;
import edu.fpt.groupfive.model.AllocationRequestDetail;
import edu.fpt.groupfive.model.ReturnRequest;
import edu.fpt.groupfive.model.ReturnRequestDetail;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReturnRequestMapper {
    ReturnRequest toReturnRequest (ReturnRequestCreateRequest dto);
    List<ReturnRequestDetail> toListReturnRequestDetail(List<ReturnRequestDetailRequest> list);

    List<ReturnRequestRespnse> toResponseList(List<ReturnRequest> list);
    ReturnRequestRespnse toRespnse(ReturnRequest request);
}

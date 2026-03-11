package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.ReturnReqDAO;
import edu.fpt.groupfive.dao.ReturnReqDetailDAO;
import edu.fpt.groupfive.dao.impl.ReturnReqDAOImpl;
import edu.fpt.groupfive.dto.request.ReturnRequestCreateRequest;
import edu.fpt.groupfive.dto.request.ReturnRequestDetailRequest;
import edu.fpt.groupfive.mapper.ReturnRequestMapper;
import edu.fpt.groupfive.model.AllocationRequestDetail;
import edu.fpt.groupfive.model.ReturnRequest;
import edu.fpt.groupfive.model.ReturnRequestDetail;
import edu.fpt.groupfive.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnReqDAO returnReqDAO;
    private final ReturnReqDetailDAO returnReqDetailDAO;
    private final ReturnRequestMapper returnRequestMapper;

    @Override
    public void createRequest(ReturnRequestCreateRequest dto) {
        // Map dữ liệu dto vào model
        ReturnRequest returnRequest = returnRequestMapper.toReturnRequest(dto);

        // Giá trị mặc định của hệ thống
        returnRequest.setStatus("PENDING_AM");
        returnRequest.setRequestDate(LocalDateTime.now());

        // Lưu Id của Allocation Request
        Integer generatedId = returnReqDAO.insert(returnRequest);

        List<ReturnRequestDetail> details = returnRequestMapper
                .toListReturnRequestDetail(dto.getDetails());

        for (ReturnRequestDetail x : details) {
            x.setRequestId(generatedId);
        }

        returnReqDetailDAO.insertBatch(generatedId, details);
    }
}

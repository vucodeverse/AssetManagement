package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.ReturnReqDAO;
import edu.fpt.groupfive.dao.ReturnReqDetailDAO;
import edu.fpt.groupfive.dao.impl.ReturnReqDAOImpl;
import edu.fpt.groupfive.dto.request.ReturnRequestCreateRequest;
import edu.fpt.groupfive.dto.request.ReturnRequestDetailRequest;
import edu.fpt.groupfive.dto.response.ReturnRequestRespnse;
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
    public List<ReturnRequestRespnse> getAllRequest(Integer departmentId) {
        return returnRequestMapper.toResponseList(returnReqDAO.findAll(departmentId));
    }

    @Override
    public ReturnRequestRespnse getRequestById(Integer id) {
        return  returnRequestMapper.toRespnse(returnReqDAO.findById(id));
    }

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

    @Override
    public void updateRequest(Integer id, ReturnRequestCreateRequest dto) {
        ReturnRequest returnRequest = returnReqDAO.findById(id);

        if (returnRequest == null) {
            throw new RuntimeException("Yêu cầu không tồn tại!");
        }

        if (!"PENDING_AM".equals(returnRequest.getStatus())) {
            throw new RuntimeException("Chỉ được sửa khi ở trạng thái PENDING_AM!");
        }

        returnRequest.setRequestReason(dto.getRequestReason());

        returnReqDAO.update(returnRequest);

        returnReqDetailDAO.deleteByRequestId(id);

        List<ReturnRequestDetail> details = returnRequestMapper
                .toListReturnRequestDetail(dto.getDetails());

        for (ReturnRequestDetail x : details) {
            x.setRequestId(id);
        }

        returnReqDetailDAO.insertBatch(id, details);
    }

    @Override
    public void deleteRequest(Integer id) {
        ReturnRequest returnRequest = returnReqDAO.findById(id);

        if (returnRequest == null) {
            throw new RuntimeException("Yêu cầu không tồn tại!");
        }

        if (!"PENDING_AM".equals(returnRequest.getStatus())) {
            throw new RuntimeException("Chỉ được xóa khi ở trạng thái PENDING_AM!");
        }

        returnReqDetailDAO.deleteByRequestId(id);
        returnReqDAO.delete(id);
    }
}

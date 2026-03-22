package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.dao.AssetHandoverDao;
import edu.fpt.groupfive.dao.AssetHandoverDetailDao;
import edu.fpt.groupfive.dao.ReturnReqDAO;
import edu.fpt.groupfive.dao.ReturnReqDetailDAO;
import edu.fpt.groupfive.dto.request.ReturnRequestCreateRequest;
import edu.fpt.groupfive.dto.response.ReturnRequestRespnse;
import edu.fpt.groupfive.mapper.ReturnRequestMapper;
import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnReqDAO returnReqDAO;
    private final ReturnReqDetailDAO returnReqDetailDAO;
    private final ReturnRequestMapper returnRequestMapper;
    private final AssetHandoverDao assetHandoverDao;
    private final AssetHandoverDetailDao assetHandoverDetailDao;

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

        //=================================================================================
        //============================ Xuống kho cho khánh ================================
        //=================================================================================

        AssetHandover handover = new AssetHandover();
        handover.setHandoverType("RETURN");
        handover.setAllocationRequestId(null);
        handover.setReturnRequestId(generatedId);
        handover.setToDepartmentId(null);
        handover.setFromDepartmentId(returnRequest.getRequestedDepartmentId());
        handover.setStatus(Status.PENDING);
        handover.setNote("Bản ghi lệnh trả tạo tự động khi có yêu cầu trả #" + generatedId);
        Integer handoverId = assetHandoverDao.insert(handover);

        List<AssetHandoverDetail> handoverDetailList = new ArrayList<>();
        for (ReturnRequestDetail reqDetail : details) {
            AssetHandoverDetail hd = new AssetHandoverDetail();
            hd.setHandoverId(handoverId);
            hd.setAssetId(reqDetail.getAssetId());
            hd.setQcReportId(null);
            hd.setNote(reqDetail.getNote());

            handoverDetailList.add(hd);

        }

        assetHandoverDetailDao.insertBatch(handoverId, handoverDetailList);

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

    @Override
    public List<ReturnRequestRespnse> searchRequest(Integer departmentId, String requestId,
                                                    String status, LocalDate fromDate, LocalDate toDate) {
        return returnRequestMapper.toResponseList(returnReqDAO.search(departmentId, requestId, status, fromDate, toDate));
    }

}
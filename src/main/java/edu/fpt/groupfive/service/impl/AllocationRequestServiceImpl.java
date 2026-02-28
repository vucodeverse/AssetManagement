package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AllocationReqDao;
import edu.fpt.groupfive.dao.AllocationReqDetailDao;
import edu.fpt.groupfive.dto.request.AllocationRequestCreateRequest;
import edu.fpt.groupfive.mapper.AllocationRequestMapper;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.model.AllocationRequestDetail;
import edu.fpt.groupfive.service.AllocationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllocationRequestServiceImpl implements AllocationRequestService {

    private final AllocationReqDao allocationReqDao;
    private final AllocationReqDetailDao allocationReqDetailDao;
    private final AllocationRequestMapper allocationRequestMapper;

    @Override
    public List<AllocationRequest> getAllAllocationRequest(Integer departmentId) {
        return allocationReqDao.findAll(departmentId);
    }

    @Override
    public void createRequest(AllocationRequestCreateRequest dto) {
        //Map dữ liệu dto vào model
        AllocationRequest request = allocationRequestMapper.toAllocationRequest(dto);
        // Giá trị mặc định của hệ thống
        request.setStatus("PENDING_AM");
        request.setRequestDate(LocalDateTime.now());

        // Lưu Id của Allocation Request
        Integer generatedId = allocationReqDao.insert(request);

        List<AllocationRequestDetail> details = allocationRequestMapper
                .toListAllocationRequestDetail(dto.getDetails());

        for (AllocationRequestDetail x : details) {
            x.setRequestId(generatedId);
        }

        // Thêm hàng loạt vào database
        allocationReqDetailDao.insertBatch(generatedId, details);

    }





}

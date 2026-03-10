package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AllocationReqDao;
import edu.fpt.groupfive.dao.AllocationReqDetailDao;
import edu.fpt.groupfive.dto.request.AllocationRequestCreateRequest;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.mapper.AllocationRequestMapper;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.model.AllocationRequestDetail;
import edu.fpt.groupfive.service.AllocationRequestService;
import edu.fpt.groupfive.dao.UserDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllocationRequestServiceImpl implements AllocationRequestService {

    private final AllocationReqDao allocationReqDao;
    private final AllocationReqDetailDao allocationReqDetailDao;
    private final AllocationRequestMapper allocationRequestMapper;
    private final UserDAO userDao;

    @Override
    public List<AllocationRequest> getAllAllocationRequest(Integer departmentId) {
        return allocationReqDao.findAll(departmentId);
    }

    @Override
    public AllocationRequestResponse getRequestById(Integer id) {

        AllocationRequest req = allocationReqDao.findById(id);

        if (req == null) {
            throw new RuntimeException("Yêu cầu không tồn tại!");
        }

        AllocationRequestResponse response = allocationRequestMapper.toResponse(req);

        if (response.getAmApprovedBy() != null) {
            userDao.findById(response.getAmApprovedBy()).ifPresent(u -> {
                String fullName = (u.getFirstName() != null ? u.getFirstName() : "") + " " +
                        (u.getLastName() != null ? u.getLastName() : "");
                response.setAmApprovedName(fullName.trim());
            });
        }

        List<AllocationRequestDetail> details = allocationReqDetailDao.findByRequestId(id);

        response.setDetails(allocationRequestMapper.toDetailResponseList(details));

        return response;
    }

    @Override
    public void createRequest(AllocationRequestCreateRequest dto) {
        // Map dữ liệu dto vào model
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

    @Override
    public void updateRequest(Integer id, AllocationRequestCreateRequest dto) {
        AllocationRequest req = allocationReqDao.findById(id);

        if (req == null) {
            throw new RuntimeException("Yêu cầu không tồn tại!");
        }

        if (!"PENDING_AM".equals(req.getStatus())) {
            throw new RuntimeException("Chỉ được sửa khi ở trạng thái PENDING_AM!");
        }

        req.setRequestReason(dto.getRequestReason());
        req.setPriority(dto.getPriority());
        req.setNeededByDate(dto.getNeededByDate());

        allocationReqDao.update(req);

        allocationReqDetailDao.deleteByRequestId(id);

        List<AllocationRequestDetail> details = allocationRequestMapper
                .toListAllocationRequestDetail(dto.getDetails());

        for (AllocationRequestDetail x : details) {
            x.setRequestId(id);
        }

        allocationReqDetailDao.insertBatch(id, details);
    }

    @Override
    public void deleteRequest(Integer id) {
        AllocationRequest req = allocationReqDao.findById(id);

        if (req == null) {
            throw new RuntimeException("Yêu cầu không tồn tại!");
        }
        if (!"PENDING_AM".equals(req.getStatus())) {
            throw new RuntimeException("Chỉ được xóa khi ở trạng thái PENDING_AM!");
        }

        allocationReqDetailDao.deleteByRequestId(id);
        allocationReqDao.delete(id);
    }

    @Override
    public void updateStatus(Integer id, String status, Integer amApprovedBy, String reasonReject) {
        allocationReqDao.updateStatus(id, status, amApprovedBy, reasonReject);
    }

    @Override
    public List<AllocationRequest> search(Integer departmentId, String requestId,
            String status, String priority, LocalDate fromDate,
            LocalDate toDate/* int offset, int size */) {
        return allocationReqDao.search(departmentId, requestId, status, priority, fromDate,
                toDate/* offset, size */);
    }

    @Override
    public int countAll(Integer departmentId) {
        return allocationReqDao.countAll(departmentId);
    }

    @Override
    public int countFiltered(Integer departmentId, String requestId, String status,
            String priority, LocalDate fromDate, LocalDate toDate) {
        return allocationReqDao.countInFilter(departmentId, requestId, status, priority, fromDate, toDate);
    }

}

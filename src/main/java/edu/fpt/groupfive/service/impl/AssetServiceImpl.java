package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.dto.request.AssetCreateRequest;
import edu.fpt.groupfive.dto.request.AssetUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.AssetResponse;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.mapper.AssetMapper;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.model.AssetType;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetDAO assetDAO;
    private final AssetTypeDAO assetTypeDAO;
    private final AssetMapper assetMapper;
    private static final int PAGE_SIZE = 5;

    // get all
    @Override
    public List<AssetResponse> getAll() {
        List<Asset> assets = assetDAO.findAll();
        return assetMapper.toAssetResponseList(assets);
    }

    // get by id
    @Override
    public AssetResponse getById(Integer id) {

        Asset asset = assetDAO.findById(id)
                .orElseThrow(() ->
                        new InvalidDataException("Không tìm thấy tài sản với id = " + id));

        return assetMapper.toAssetResponse(asset);
    }

    @Override
    public List<AssetResponse> getAllByDepartmentId(Integer departmentId) {
        List<Asset> assets = assetDAO.findAllByDepartmentId(departmentId);
        return assetMapper.toAssetResponseList(assets);
    }


    // create
    @Override
    @Transactional
    public void create(AssetCreateRequest request) {
        Integer quantity = request.getQuantity();

        //ktra số lượng phản >=1
        if (quantity == null || quantity < 1) {
            throw new InvalidDataException("Số lượng phải >= 1");
        }


        //  Validate assetType
        AssetType type = assetTypeDAO.findById(request.getAssetTypeId());
        if (type == null) {
            throw new InvalidDataException("Loại tài sản không tồn tại");
        }

        //  Validate tiền không âm
        validateOriginalCost(request.getOriginalCost());

        // Validate date logic
        validateDateLogic(
                request.getWarrantyStartDate(),
                request.getWarrantyEndDate(),
                request.getAcquisitionDate()
        );


        for (int i = 0; i < quantity; i++) {
            Asset asset = assetMapper.toAsset(request);
            assetDAO.insert(asset);

        }
    }

    // update
    @Override
    @Transactional
    public void update(Integer id, AssetUpdateRequest request) {

        Asset existing = assetDAO.findById(id)
                .orElseThrow(() ->
                        new InvalidDataException("Không tìm thấy tài sản với id = " + id));




        // Validate assetType nếu có đổi
        if (request.getAssetTypeId() != null) {
            AssetType type = assetTypeDAO.findById(request.getAssetTypeId());
            if (type == null) {
                throw new InvalidDataException("Loại tài sản không tồn tại");
            }
        }

        validateOriginalCost(request.getOriginalCost());

        validateDateLogic(
                request.getWarrantyStartDate(),
                request.getWarrantyEndDate(),
                request.getAcquisitionDate()
        );

        assetMapper.updateFromRequest(request, existing);

        assetDAO.update(existing);
    }

    //DELETE
    @Override
    @Transactional
    public void delete(Integer id) {

        Asset existing = assetDAO.findById(id)
                .orElseThrow(() ->
                        new InvalidDataException("Không tìm thấy tài sản"));

        // Business rule: chỉ cho xóa khi status NEW
        if (existing.getCurrentStatus() != (AssetStatus.NEW)) {
            throw new InvalidDataException(
                    "Chỉ có thể xóa tài sản ở trạng thái NEW");
        }

        assetDAO.delete(id);
    }

    @Override
    public AssetDetailResponse getDetailById(Integer id) {
        return assetDAO.findDetailById(id)
                .orElseThrow(() ->
                        new InvalidDataException("Không tìm thấy tài sản với id = " + id));
    }


    @Override
    public PageResponse<AssetResponse> searchAssets(
            String keyword,
            AssetStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            String direction,
            int page
    ) {

        if (page < 1) {
            page = 1;
        }

        int offset = (page - 1) * PAGE_SIZE;

        var assets = assetDAO.searchAssets(keyword, status, fromDate, toDate, direction, offset, PAGE_SIZE);

        int total = assetDAO.countAssets(
                keyword,
                status,
                fromDate, toDate
        );

        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

        var responses = assetMapper.toAssetResponseList(assets);

        return new PageResponse<>(responses, page, PAGE_SIZE, total, totalPages);
    }

    // validate

    private void validateOriginalCost(BigDecimal cost) {
        if (cost != null && cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Giá gốc không được âm");
        }
    }

    private void validateDateLogic(
            java.time.LocalDate warrantyStart,
            java.time.LocalDate warrantyEnd,
            java.time.LocalDate acquisitionDate
    ) {

        if (warrantyStart != null && warrantyEnd != null) {
            if (warrantyEnd.isBefore(warrantyStart)) {
                throw new InvalidDataException(
                        "Ngày hết bảo hành phải sau ngày bắt đầu bảo hành");
            }
        }

        if (acquisitionDate != null && warrantyStart != null) {
            if (warrantyStart.isBefore(acquisitionDate)) {
                throw new InvalidDataException(
                        "Ngày bảo hành không thể trước ngày nhập kho");
            }
        }
    }
}
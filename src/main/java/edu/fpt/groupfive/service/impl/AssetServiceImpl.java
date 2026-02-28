package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.dto.request.AssetCreateRequest;
import edu.fpt.groupfive.dto.request.AssetUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetResponse;
import edu.fpt.groupfive.mapper.AssetMapper;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.model.AssetType;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetDAO assetDAO;
    private final AssetTypeDAO assetTypeDAO;
    private final AssetMapper assetMapper;

    // ================= GET ALL =================
    @Override
    public List<AssetResponse> getAll() {
        List<Asset> assets = assetDAO.findAll();
        return assetMapper.toResponseList(assets);
    }

    // ================= GET BY ID =================
    @Override
    public AssetResponse getById(Integer id) {

        Asset asset = assetDAO.findById(id)
                .orElseThrow(() ->
                        new InvalidDataException("Không tìm thấy tài sản với id = " + id));

        return assetMapper.toResponse(asset);
    }

    // ================= CREATE =================
    @Override
    @Transactional
    public void create(AssetCreateRequest request) {

        // Validate serial trùng
        if (request.getSerialNumber() != null &&
                assetDAO.existsBySerial(request.getSerialNumber())) {
            throw new InvalidDataException("Serial number đã tồn tại");
        }

        //  Validate assetType tồn tại
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

        // Map sang entity
        Asset asset = assetMapper.toAsset(request);

        // default status khi tạo mới
        asset.setCurrentStatus("NEW");

        assetDAO.insert(asset);
    }

    // ================= UPDATE =================
    @Override
    @Transactional
    public void update(Integer id, AssetUpdateRequest request) {

        Asset existing = assetDAO.findById(id)
                .orElseThrow(() ->
                        new InvalidDataException("Không tìm thấy tài sản"));

        // Nếu đổi serial → check trùng
        if (request.getSerialNumber() != null &&
                !request.getSerialNumber().equals(existing.getSerialNumber())) {

            if (assetDAO.existsBySerial(request.getSerialNumber())) {
                throw new InvalidDataException("Serial number đã tồn tại");
            }
        }

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

    // ================= DELETE =================
    @Override
    @Transactional
    public void delete(Integer id) {

        Asset existing = assetDAO.findById(id)
                .orElseThrow(() ->
                        new InvalidDataException("Không tìm thấy tài sản"));

        // Business rule: chỉ cho xóa khi status NEW
        if (!"NEW".equalsIgnoreCase(existing.getCurrentStatus())) {
            throw new InvalidDataException(
                    "Chỉ có thể xóa tài sản ở trạng thái NEW");
        }

        assetDAO.delete(id);
    }

    // ================= PRIVATE VALIDATION =================

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
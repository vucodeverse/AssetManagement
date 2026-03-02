package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.dao.impl.AssetTypeDAOImpl;
import edu.fpt.groupfive.dto.request.AssetTypeCreateRequest;
import edu.fpt.groupfive.dto.request.AssetTypeUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.mapper.AssetTypeMapper;
import edu.fpt.groupfive.model.AssetType;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetTypeServiceImpl implements AssetTypeService {

    private final AssetTypeDAO assetTypeDAO;
    private final AssetTypeMapper assetTypeMapper;

    @Override
    public List<AssetTypeResponse> getAll() {
        List<AssetType> assetTypes = assetTypeDAO.findAll();
        return assetTypeMapper.toAssetTypeResponseList(assetTypes);
    }

    @Override
    public List<AssetTypeResponse> getAllAssetType() {
        return assetTypeDAO.findAll().stream().map(assetType -> AssetTypeResponse.builder()
                .typeId(assetType.getTypeId())
                .typeName(assetType.getTypeName()).build()).collect(Collectors.toList());
    }

    public AssetTypeResponse getById(Integer id) {
        AssetType assetType = assetTypeDAO.findById(id);
        if (assetType == null) {
            throw new InvalidDataException("Không tìm thấy loại tài sản với id = : " + id);
        }
        return assetTypeMapper.toAssetTypeResponse(assetType);
    }

    @Override
    public Optional<AssetType> findById(Integer assetTypeId) {

        return Optional.empty();
    }

    @Transactional
    public void create(AssetTypeCreateRequest request) {
        String name = request.getTypeName().trim();
        if (assetTypeDAO.existByTypeName(name)) {
            throw new InvalidDataException("Ten loai tai san da ton tai");
        }
        AssetType assetType = assetTypeMapper.toAssetType(request);
        assetType.setTypeName(name);
        assetType.setStatus("ACTIVE");
        assetTypeDAO.insert(assetType);
    }

    @Override
    public String findNameById(Integer assetTypeId) {
        return assetTypeDAO.findById(assetTypeId).getTypeName();
    }

    @Transactional
    public void update(AssetTypeUpdateRequest request) {
        AssetType existing = assetTypeDAO.findById(request.getTypeId());
        if (existing == null) {
            throw new InvalidDataException("Không tìm thấy loại tài sản với id = " + request.getTypeId());
        }
        String newName = request.getTypeName().trim();
        String oldName = existing.getTypeName();
        // neu name moi khac name cu thi check trung
        if (!oldName.equalsIgnoreCase(newName)) {
            if (assetTypeDAO.existByTypeName(newName)) {
                throw new InvalidDataException("Tên loại tài sản đã tồn tại");
            }
        }
        assetTypeMapper.updateFromRequest(request, existing);
        existing.setTypeName(newName);
        assetTypeDAO.update(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        AssetType existing = assetTypeDAO.findById(id);
        if (existing == null) {
            throw new InvalidDataException("Không tìm thấy loại tài sản với id = " + id);
        }

        // check asset đang sử dụng
        if (assetTypeDAO.existAssetUsingType(id)) {
            throw new InvalidDataException(
                    "Không thể xóa. Loại tài sản đã được sử dụng");
        }
        assetTypeDAO.delete(id);
    }

    @Override
    public Map<Integer, String> getAssetTypeIdToNameMap() {
        return assetTypeDAO.findAll().stream()
                .collect(Collectors.toMap(AssetType::getTypeId, AssetType::getTypeName,
                        (existing, replacement) -> existing));
    }
}

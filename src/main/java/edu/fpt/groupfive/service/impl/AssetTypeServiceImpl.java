package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.dao.impl.AssetTypeDAOImpl;
import edu.fpt.groupfive.dto.request.AssetTypeCreateRequest;
import edu.fpt.groupfive.dto.request.AssetTypeUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.mapper.AssetTypeMapper;
import edu.fpt.groupfive.model.AssetType;
import edu.fpt.groupfive.service.AssetTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public AssetTypeResponse getById(Integer id) {
        AssetType assetType = assetTypeDAO.findById(id);
        if (assetType == null) {
            throw new RuntimeException("Khong tim thay loai tai san voi id = : " + id);
        }
        return assetTypeMapper.toAssetTypeResponse(assetType);
    }

    @Override
    @Transactional
    public void create(AssetTypeCreateRequest request) {
        AssetType assetType = assetTypeMapper.toAssetType(request);
        assetType.setStatus("ACTIVE");
        assetTypeDAO.insert(assetType);
    }

    @Override
    @Transactional
    public void update(AssetTypeUpdateRequest request) {
        AssetType existing = assetTypeDAO.findById(request.getTypeId());
        if (existing == null) {
            throw new RuntimeException("Khong tim thay loai tai san voi id = " + request.getTypeId());
        }
        assetTypeMapper.updateFromRequest(request, existing);
        assetTypeDAO.update(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        AssetType existing = assetTypeDAO.findById(id);
        if (existing == null) {
            throw new RuntimeException("Khong tim thay loai tai san voi id = " +id);
        }
        assetTypeDAO.delete(id);
    }
}

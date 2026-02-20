package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.model.AssetType;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetTypeServiceImpl implements AssetTypeService {

    private final AssetTypeDAO assetTypeDAO;

    @Override
    public List<AssetTypeResponse> getAllAssetType() {
        return assetTypeDAO.findAll().stream().map(assetType -> AssetTypeResponse.builder()
                .typeId(assetType.getTypeId())
                .typeName(assetType.getTypeName()).build()
        ).collect(Collectors.toList());
    }

    @Override
    public Optional<AssetType> findById(Integer assetTypeId) {


        return Optional.empty();
    }

    @Override
    public String findNameById(Integer assetTypeId) {
        return assetTypeDAO.findById(assetTypeId);
    }
}

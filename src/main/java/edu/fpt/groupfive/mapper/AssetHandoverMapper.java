package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.response.AssetHandoverResponse;
import edu.fpt.groupfive.model.AssetHandover;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetHandoverMapper {

    AssetHandoverResponse toResponse(AssetHandover entity);

    List<AssetHandoverResponse> toResponseList(List<AssetHandover> list);
}

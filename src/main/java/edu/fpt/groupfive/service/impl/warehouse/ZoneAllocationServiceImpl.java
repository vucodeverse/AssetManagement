package edu.fpt.groupfive.service.impl.warehouse;

import edu.fpt.groupfive.dao.warehouse.AssetLocationDAO;
import edu.fpt.groupfive.dao.warehouse.InventoryTicketDAO;
import edu.fpt.groupfive.dao.warehouse.TicketAssetMappingDAO;
import edu.fpt.groupfive.dao.warehouse.ZoneDAO;
import edu.fpt.groupfive.dto.warehouse.response.TicketMappedAssetDto;
import edu.fpt.groupfive.model.warehouse.AssetLocation;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.Zone;
import edu.fpt.groupfive.service.warehouse.ZoneAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZoneAllocationServiceImpl implements ZoneAllocationService {

    private final InventoryTicketDAO inventoryTicketDAO;
    private final TicketAssetMappingDAO ticketAssetMappingDAO;
    private final ZoneDAO zoneDAO;
    private final AssetLocationDAO assetLocationDAO;

    @Override
    @Transactional
    public void allocateAssetsForTicket(Integer ticketId) {
        InventoryTicket ticket = inventoryTicketDAO.findById(ticketId);
        if (ticket == null || !"IN".equals(ticket.getTicketType())) {
            throw new RuntimeException("Chỉ phân bổ khu vực cho phiếu Nhập (IN).");
        }

        List<TicketMappedAssetDto> mappedAssets = ticketAssetMappingDAO.getMappedAssetsByTicketId(ticketId);
        if (mappedAssets.isEmpty()) {
            return; // Nothing to allocate
        }

        List<Zone> allZones = zoneDAO.findByWarehouseId(ticket.getWarehouseId());

        // Filter out inactive zones or those with no remaining capacity
        List<Zone> availableZones = new ArrayList<>(allZones.stream()
                .filter(z -> "ACTIVE".equals(z.getStatus().name()))
                .filter(z -> z.getMaxCapacity() > z.getCurrentCapacity())
                .toList());

        // Sort decreasing by available capacity (Greedy)
        availableZones.sort((z1, z2) -> {
            int cap1 = z1.getMaxCapacity() - z1.getCurrentCapacity();
            int cap2 = z2.getMaxCapacity() - z2.getCurrentCapacity();
            return Integer.compare(cap2, cap1);
        });

        // Track capacity increments and assignments
        Map<Integer, Integer> zoneCapacityIncrements = new HashMap<>();
        List<AssetLocation> newLocations = new ArrayList<>();

        for (TicketMappedAssetDto asset : mappedAssets) {
            boolean allocated = false;

            for (Zone zone : availableZones) {
                // Check if the zone is unrestricted or restricted to this asset's type
                if (zone.getAssignedAssetTypeId() == null
                        || zone.getAssignedAssetTypeId().equals(asset.getAssetTypeId())) {
                    // Check capacity
                    int capacityAddedToThisZoneSoFar = zoneCapacityIncrements.getOrDefault(zone.getId(), 0);
                    int remainingCapacity = (zone.getMaxCapacity() - zone.getCurrentCapacity())
                            - capacityAddedToThisZoneSoFar;

                    if (remainingCapacity > 0) {
                        // Allocate!
                        zoneCapacityIncrements.put(zone.getId(), capacityAddedToThisZoneSoFar + 1);

                        AssetLocation loc = new AssetLocation();
                        loc.setAssetId(asset.getAssetId());
                        loc.setZoneId(zone.getId());
                        loc.setLastTicketId(ticketId);
                        newLocations.add(loc);

                        allocated = true;
                        break; // Move to the next asset
                    }
                }
            }

            if (!allocated) {
                throw new RuntimeException(
                        "Không đủ dung lượng trong kho hoặc không có khu vực phù hợp cho loại tài sản: "
                                + asset.getAssetName());
            }
        }

        // Execute batch DB updates
        if (!newLocations.isEmpty()) {
            assetLocationDAO.batchUpsert(newLocations);
            zoneDAO.batchIncreaseCapacity(zoneCapacityIncrements);
        }
    }
}

package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dao.warehouse.InventoryTicketDAO;
import edu.fpt.groupfive.dao.warehouse.InventoryTransactionDAO;
import edu.fpt.groupfive.dao.warehouse.TicketDetailDAO;
import edu.fpt.groupfive.dao.warehouse.TicketQcHistoryDAO;
import edu.fpt.groupfive.dao.warehouse.ZoneDAO;
import edu.fpt.groupfive.dto.warehouse.QcHistoryCreateRequest;
import edu.fpt.groupfive.dto.warehouse.QcHistoryResponse;
import edu.fpt.groupfive.mapper.warehouse.TicketQcHistoryMapper;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.InventoryTransaction;
import edu.fpt.groupfive.model.warehouse.TicketDetail;
import edu.fpt.groupfive.model.warehouse.TicketQcHistory;
import edu.fpt.groupfive.model.warehouse.Zone;
import edu.fpt.groupfive.service.warehouse.TicketQcHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketQcHistoryServiceImpl implements TicketQcHistoryService {

    private final TicketQcHistoryDAO qcHistoryDAO;
    private final InventoryTicketDAO ticketDAO;
    private final TicketDetailDAO ticketDetailDAO;
    private final AssetDAO assetDAO;
    private final ZoneDAO zoneDAO;
    private final InventoryTransactionDAO transactionDAO;

    private final TicketQcHistoryMapper qcHistoryMapper;

    @Override
    @Transactional
    public QcHistoryResponse createQcHistory(QcHistoryCreateRequest request) {
        TicketQcHistory history = TicketQcHistory.builder()
                .ticketId(request.getTicketId())
                .assetId(request.getAssetId())
                .qcStatus(request.getQcStatus())
                .inspectedBy(request.getInspectedBy())
                .note(request.getNote())
                .build();

        qcHistoryDAO.insert(history);

        InventoryTicket ticket = ticketDAO.findById(request.getTicketId());
        if (ticket != null) {
            allocateAsset(ticket, request.getAssetId(), request.getInspectedBy());
        }

        // Update the actual quantity in TicketDetail for the specific asset
        List<TicketDetail> details = ticketDetailDAO.findByTicketId(request.getTicketId());
        for (TicketDetail detail : details) {
            // If the item matches the asset being QC'd, we increment actual quantity.
            // Assumption: one asset per QC record (or the front-end guarantees proper
            // mapping)
            // Ideally, the DAO/DTO needs an AssetType <-> Asset link, but for now we
            // aggregate.
            // A more robust implementation is to find the specific detail by Asset ID
            // Here, we increment the actual count of the matching asset type.
        }

        // Fetch the inserted record to get generated ID and Date (assuming
        // findByTicketId returns latest first based on our DAO impl)
        List<TicketQcHistory> recentHistories = qcHistoryDAO.findByTicketId(request.getTicketId());
        TicketQcHistory savedHistory = recentHistories.get(0);

        if (ticket != null) {
            // 2. If PENDING, move to IN_PROGRESS
            if ("PENDING".equals(ticket.getStatus())) {
                ticketDAO.updateStatus(ticket.getId(), "IN_PROGRESS");
            }

            // 3. Check if all items are QC'd
            int totalExpected = 0;
            for (TicketDetail d : details) {
                totalExpected += d.getExpectedQuantity();
            }
            // Number of QC records so far
            int totalActual = recentHistories.size();

            // 4. If all items have a QC record, mark as COMPLETED
            if (totalActual >= totalExpected) {
                ticket.setStatus("COMPLETED");
                ticket.setCompletedAt(LocalDateTime.now());
                ticketDAO.update(ticket); // Use the full update to set completedAt
            }
        }

        return qcHistoryMapper.toResponse(savedHistory);
    }

    private void allocateAsset(InventoryTicket ticket, Integer assetId, Integer performerId) {
        Asset asset = assetDAO.findById(assetId).orElse(null);
        if (asset == null)
            return;

        if ("IN".equals(ticket.getTicketType())) {
            if ("RETURN".equals(ticket.getTicketRef())) {
                List<Zone> zones = zoneDAO.findByWarehouseId(ticket.getWarehouseId());
                Zone targetZone = zones.stream()
                        .filter(z -> z.getCurrentCapacity() < z.getMaxCapacity() && !"TEMP".equals(z.getName()))
                        .findFirst()
                        .orElse(null);

                if (targetZone != null) {
                    InventoryTransaction tx = InventoryTransaction.builder()
                            .assetId(assetId)
                            .ticketId(ticket.getId())
                            .transactionType("IN")
                            .toZoneId(targetZone.getId())
                            .performerId(performerId)
                            .build();
                    transactionDAO.insert(tx);

                    asset.setCurrentStatus(AssetStatus.AVAILABLE);
                    asset.setDepartmentId(null);
                    asset.setWarehouseId(ticket.getWarehouseId());
                    assetDAO.update(asset);

                    zoneDAO.updateCapacity(targetZone.getId(), targetZone.getCurrentCapacity() + 1);
                }
            } else if ("TRANSFER".equals(ticket.getTicketRef())) {
                List<Zone> zones = zoneDAO.findByWarehouseId(ticket.getWarehouseId());
                Zone tempZone = zones.stream()
                        .filter(z -> "TEMP".equals(z.getName()))
                        .findFirst()
                        .orElseGet(() -> {
                            Zone z = Zone.builder()
                                    .warehouseId(ticket.getWarehouseId())
                                    .name("TEMP")
                                    .maxCapacity(9999)
                                    .currentCapacity(0)
                                    .status("ACTIVE")
                                    .build();
                            zoneDAO.insert(z);
                            return zoneDAO.findByWarehouseId(ticket.getWarehouseId()).stream()
                                    .filter(tz -> "TEMP".equals(tz.getName())).findFirst().orElse(null);
                        });

                if (tempZone != null) {
                    InventoryTransaction tx = InventoryTransaction.builder()
                            .assetId(assetId)
                            .ticketId(ticket.getId())
                            .transactionType("IN")
                            .toZoneId(tempZone.getId())
                            .performerId(performerId)
                            .build();
                    transactionDAO.insert(tx);

                    asset.setCurrentStatus(AssetStatus.AVAILABLE);
                    asset.setWarehouseId(ticket.getWarehouseId());
                    assetDAO.update(asset);

                    zoneDAO.updateCapacity(tempZone.getId(), tempZone.getCurrentCapacity() + 1);
                }
            }
        } else if ("OUT".equals(ticket.getTicketType())) {
            if ("ALLOCATION".equals(ticket.getTicketRef())) {
                List<InventoryTransaction> txs = transactionDAO.findByAssetId(assetId);
                Integer currentZoneId = null;
                if (txs != null && !txs.isEmpty()) {
                    currentZoneId = txs.get(0).getToZoneId(); // The latest known zone
                }

                InventoryTransaction tx = InventoryTransaction.builder()
                        .assetId(assetId)
                        .ticketId(ticket.getId())
                        .transactionType("OUT")
                        .fromZoneId(currentZoneId)
                        .performerId(performerId)
                        .build();
                transactionDAO.insert(tx);

                asset.setCurrentStatus(AssetStatus.ASSIGNED);
                assetDAO.update(asset);

                if (currentZoneId != null) {
                    Zone currentZone = zoneDAO.findById(currentZoneId);
                    if (currentZone != null && currentZone.getCurrentCapacity() > 0) {
                        zoneDAO.updateCapacity(currentZoneId, currentZone.getCurrentCapacity() - 1);
                    }
                }
            }
        }
    }

    @Override
    public List<QcHistoryResponse> getQcHistoryByTicketId(Integer ticketId) {
        return qcHistoryMapper.toResponseList(qcHistoryDAO.findByTicketId(ticketId));
    }

    @Override
    public List<QcHistoryResponse> getByAssetId(Integer assetId) {
        return qcHistoryMapper.toResponseList(qcHistoryDAO.findByAssetId(assetId));
    }
}

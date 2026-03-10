package edu.fpt.groupfive.service.warehouse;

public interface ZoneAllocationService {
    /**
     * Allocates mapped physical assets to appropriate valid zones.
     * Uses a greedy algorithm to fill up zones with available capacity.
     * Throws an exception if not enough total warehouse capacity.
     */
    void allocateAssetsForTicket(Integer ticketId);
}

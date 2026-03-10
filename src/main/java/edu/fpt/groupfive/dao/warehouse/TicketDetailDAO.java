package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.TicketDetail;

import java.util.List;

public interface TicketDetailDAO {
    List<TicketDetail> findByTicketId(Integer ticketId);

    void insert(TicketDetail detail);

    void insertBatch(List<TicketDetail> details);

    void update(TicketDetail detail);

    void delete(Integer id);

    void deleteByTicketId(Integer ticketId);
}

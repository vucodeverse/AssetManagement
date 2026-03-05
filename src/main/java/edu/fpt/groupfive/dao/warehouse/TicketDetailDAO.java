package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.TicketDetail;
import java.util.List;

public interface TicketDetailDAO {
    int insert(TicketDetail detail);

    int update(TicketDetail detail);

    List<TicketDetail> findByTicketId(Integer ticketId);
}

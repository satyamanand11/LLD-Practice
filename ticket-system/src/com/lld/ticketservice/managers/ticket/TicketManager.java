package com.lld.ticketservice.managers.ticket;

import com.lld.ticketservice.domain.ticket.Ticket;

public interface TicketManager {
    void save(Ticket t);

    Ticket getById(String ticketId);

    Ticket getByBookingId(int bookingId);

    void updateStatus(String ticketId, String status);
}

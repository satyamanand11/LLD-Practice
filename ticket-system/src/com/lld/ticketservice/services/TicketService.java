package com.lld.ticketservice.services;

import com.lld.ticketservice.domain.ticket.Ticket;
import com.lld.ticketservice.domain.ticket.TicketStatus;
import com.lld.ticketservice.managers.ticket.TicketManager;

import java.util.UUID;

public class TicketService {
    private final TicketManager tickets;

    public TicketService(TicketManager tickets) {
        this.tickets = tickets;
    }

    public Ticket issue(int bookingId, int price, String userId) {
        Ticket t = new Ticket(UUID.randomUUID().toString(), bookingId, price, userId, TicketStatus.ACTIVE);
        tickets.save(t);
        return t;
    }

    public Ticket getByBookingId(int bookingId) {
        return tickets.getByBookingId(bookingId);
    }
}

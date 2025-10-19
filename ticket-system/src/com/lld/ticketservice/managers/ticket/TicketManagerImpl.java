package com.lld.ticketservice.managers.ticket;

import com.lld.ticketservice.domain.ticket.Ticket;
import com.lld.ticketservice.domain.ticket.TicketStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TicketManagerImpl implements TicketManager {
    private final Map<String, Ticket> byId = new ConcurrentHashMap<>();
    private final Map<Integer, String> byBooking = new ConcurrentHashMap<>();

    public void save(Ticket t) {
        byId.put(t.getTicketId(), t);
        byBooking.put(t.getBookingId(), t.getTicketId());
    }

    public Ticket getById(String id) {
        return byId.get(id);
    }

    public Ticket getByBookingId(int bookingId) {
        String tid = byBooking.get(bookingId);
        return tid == null ? null : byId.get(tid);
    }

    public void updateStatus(String id, String status) {
        Ticket t = byId.get(id);
        if (t != null) t.setStatus(TicketStatus.valueOf(status));
    }
}

package com.lld.ticketservice.domain.ticket;

public class Ticket {
    private final String ticketId;     // generated
    private final int bookingId;
    private final int price;           // total amount
    private final String userId;
    private TicketStatus status;

    public Ticket(String ticketId, int bookingId, int price, String userId, TicketStatus status) {
        this.ticketId = ticketId;
        this.bookingId = bookingId;
        this.price = price;
        this.userId = userId;
        this.status = status;
    }

    public String getTicketId() {
        return ticketId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getPrice() {
        return price;
    }

    public String getUserId() {
        return userId;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus s) {
        this.status = s;
    }
}

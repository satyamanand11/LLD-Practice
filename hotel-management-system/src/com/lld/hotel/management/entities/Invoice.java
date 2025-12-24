package com.lld.hotel.management.entities;

import java.math.BigDecimal;

public class Invoice {

    private final int invoiceId;

    private final int bookingId;

    private BigDecimal totalAmount;
    private InvoiceStatus status;

    public Invoice(int invoiceId, int bookingId, BigDecimal totalAmount) {

        if (invoiceId <= 0) {
            throw new IllegalArgumentException("invoiceId must be positive");
        }
        if (bookingId <= 0) {
            throw new IllegalArgumentException("bookingId must be positive");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("totalAmount must be non-negative");
        }

        this.invoiceId = invoiceId;
        this.bookingId = bookingId;
        this.totalAmount = totalAmount;
        this.status = InvoiceStatus.CREATED;
    }

    public void updateTotalAmount(BigDecimal newAmount) {
        if (status == InvoiceStatus.FINALIZED) {
            throw new IllegalStateException(
                    "Cannot modify a finalized invoice"
            );
        }
        if (newAmount == null || newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("totalAmount must be non-negative");
        }
        this.totalAmount = newAmount;
    }

    public void finalizeInvoice() {
        if (status == InvoiceStatus.FINALIZED) {
            throw new IllegalStateException("Invoice already finalized");
        }
        this.status = InvoiceStatus.FINALIZED;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public boolean isFinalized() {
        return status == InvoiceStatus.FINALIZED;
    }
}

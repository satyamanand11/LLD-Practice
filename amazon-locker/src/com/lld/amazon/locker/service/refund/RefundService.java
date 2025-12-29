package com.lld.amazon.locker.service.refund;

public interface RefundService {
    void refundCustomer(String customerId, String referenceId, String reason);
    void applyReturnRefundPolicy(String customerId, String returnId); // simplified for interview
}

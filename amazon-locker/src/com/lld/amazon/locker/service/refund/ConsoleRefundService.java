package com.lld.amazon.locker.service.refund;

public class ConsoleRefundService implements RefundService {

    @Override
    public void refundCustomer(String customerId, String referenceId, String reason) {
        System.out.println("[REFUND][" + customerId + "] ref=" + referenceId + " reason=" + reason);
    }

    @Override
    public void applyReturnRefundPolicy(String customerId, String returnId) {
        System.out.println("[RETURN-REFUND-POLICY][" + customerId + "] returnId=" + returnId + " policyApplied=true");
    }
}

package com.lld.amazon.locker.system;

import com.lld.amazon.locker.dto.DeliveryReceipt;
import com.lld.amazon.locker.dto.PackageRequest;
import com.lld.amazon.locker.dto.ReturnReceipt;

import java.util.List;

public interface LockerSystem {

    List<DeliveryReceipt> deliverOrderToLocation(String orderId,
                                                 String customerId,
                                                 String preferredLocationId,
                                                 List<PackageRequest> packages);

    void pickupDelivery(String assignmentId, String customerId, String customerPin);

    ReturnReceipt initiateReturnDropOff(String returnId,
                                        String customerId,
                                        String preferredLocationId,
                                        PackageRequest returnPackage);

    void pickupReturnByLogistics(String assignmentId, String logisticsCode);
}
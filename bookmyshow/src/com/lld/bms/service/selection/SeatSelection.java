package com.lld.bms.service.selection;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public final class SeatSelection {
    private final String showSeatId;
    private final List<AddOn> addOns;

    public SeatSelection(String showSeatId) {
        this(showSeatId, List.of());
    }

    public SeatSelection(String showSeatId, List<AddOn> addOns) {
        this.showSeatId = Objects.requireNonNull(showSeatId, "showSeatId cannot be null");
        if (showSeatId.isBlank()) {
            throw new IllegalArgumentException("showSeatId cannot be blank");
        }
        Objects.requireNonNull(addOns, "addOns cannot be null");
        for (AddOn a : addOns) {
            Objects.requireNonNull(a, "addOn entry cannot be null");
        }
        this.addOns = List.copyOf(addOns);
    }

    public String showSeatId() {
        return showSeatId;
    }

    public List<AddOn> addOns() {
        return Collections.unmodifiableList(addOns);
    }

    public String description() {
        if (addOns.isEmpty()) {
            return "Seat[" + showSeatId + "]";
        }
        String addOnDesc = addOns.stream()
                .map(a -> a.name() + " (" + a.price() + ")")
                .collect(Collectors.joining(" + "));
        return "Seat[" + showSeatId + "] + " + addOnDesc;
    }
}

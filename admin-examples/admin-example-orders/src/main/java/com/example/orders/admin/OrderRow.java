package com.example.orders.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Read-only order data used by the standalone module example. */
public record OrderRow(String number, String customer, String status, BigDecimal total, LocalDate placedOn) {
    public OrderRow {
        number = requireText(number, "number");
        customer = requireText(customer, "customer");
        status = requireText(status, "status");
        total = Objects.requireNonNull(total, "total");
        placedOn = Objects.requireNonNull(placedOn, "placedOn");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}

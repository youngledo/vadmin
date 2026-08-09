package com.example.orders.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.github.vaadinadminstarter.contracts.navigation.PagedQuery;
import io.github.vaadinadminstarter.contracts.navigation.PagedResult;

/** Deterministic, read-only query service that demonstrates a module-owned application boundary. */
public final class OrderQueryService {
    private final List<OrderRow> orders;

    private OrderQueryService(List<OrderRow> orders) {
        this.orders = List.copyOf(orders);
    }

    public static OrderQueryService demo() {
        return new OrderQueryService(List.of(
                new OrderRow("ORD-1001", "Acme Supplies", "Processing", new BigDecimal("1299.00"),
                        LocalDate.of(2026, 8, 1)),
                new OrderRow("ORD-1002", "Northstar Retail", "Shipped", new BigDecimal("486.50"),
                        LocalDate.of(2026, 8, 3)),
                new OrderRow("ORD-1003", "Contoso Studio", "Completed", new BigDecimal("874.20"),
                        LocalDate.of(2026, 8, 5))));
    }

    public PagedResult<OrderRow> orders(PagedQuery query) {
        Objects.requireNonNull(query, "query");
        var sorted = orders.stream().sorted(ordering(query)).toList();
        var offset = Math.multiplyExact(query.page(), query.pageSize());
        if (offset >= sorted.size()) {
            return new PagedResult<>(List.of(), sorted.size());
        }
        var endExclusive = Math.min(offset + query.pageSize(), sorted.size());
        return new PagedResult<>(sorted.subList(offset, endExclusive), sorted.size());
    }

    private static Comparator<OrderRow> ordering(PagedQuery query) {
        var comparator = switch (query.sortField()) {
            case "customer" -> Comparator.comparing(OrderRow::customer);
            case "placedOn" -> Comparator.comparing(OrderRow::placedOn);
            case "status" -> Comparator.comparing(OrderRow::status);
            case "total" -> Comparator.comparing(OrderRow::total);
            default -> Comparator.comparing(OrderRow::number);
        };
        return query.ascending() ? comparator : comparator.reversed();
    }
}

package io.github.vaadinadminstarter.contracts.navigation;

import java.util.Map;

public record PagedQuery(int page, int pageSize, String sortField, boolean ascending, Map<String, String> filters) {
    public PagedQuery { if (page < 0 || pageSize < 1) throw new IllegalArgumentException("invalid page"); filters = Map.copyOf(filters); }
}

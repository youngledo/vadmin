package io.github.youngledo.vadmin.contracts.navigation;

import java.util.List;

public record PagedResult<T>(List<T> items, long total) { public PagedResult { items = List.copyOf(items); } }

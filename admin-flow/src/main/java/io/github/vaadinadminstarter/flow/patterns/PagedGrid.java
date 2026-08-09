package io.github.vaadinadminstarter.flow.patterns;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import io.github.vaadinadminstarter.contracts.navigation.PagedQuery;
import io.github.vaadinadminstarter.contracts.navigation.PagedResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Binds a Grid to a server-side page loader using the shared paging contract. */
public final class PagedGrid<T> {
    @FunctionalInterface
    public interface PageLoader<T> {
        PagedResult<T> load(PagedQuery query);
    }

    private final Grid<T> grid;
    private final PageLoader<T> loader;
    private final Supplier<Map<String, String>> filters;
    private final Map<PagedQuery, PagedResult<T>> cache = new HashMap<>();

    public PagedGrid(Grid<T> grid, PageLoader<T> loader, String defaultSortField) {
        this(grid, loader, () -> Map.of(), defaultSortField);
    }

    public PagedGrid(Grid<T> grid, PageLoader<T> loader, Supplier<Map<String, String>> filters,
                     String defaultSortField) {
        this.grid = Objects.requireNonNull(grid);
        this.loader = Objects.requireNonNull(loader);
        this.filters = Objects.requireNonNull(filters);
        grid.setPageSize(50);
        grid.setEmptyStateText(grid.getTranslation("flow.grid.empty"));
        grid.setItems(query -> load(toPagedQuery(query, defaultSortField)).items().stream(),
                query -> Math.toIntExact(load(toPagedQuery(query, defaultSortField)).total()));
    }

    PagedResult<T> load(PagedQuery query) {
        return cache.computeIfAbsent(query, loader::load);
    }

    public void refresh() {
        cache.clear();
        grid.getDataProvider().refreshAll();
    }

    private PagedQuery toPagedQuery(Query<T, ?> query, String defaultSortField) {
        var sort = query.getSortOrders().stream().findFirst();
        var sortField = sort.map(order -> order.getSorted()).orElse(defaultSortField);
        var ascending = sort.map(order -> order.getDirection() == SortDirection.ASCENDING).orElse(true);
        return new PagedQuery(query.getOffset() / query.getLimit(), query.getLimit(), sortField, ascending,
                filters.get());
    }
}

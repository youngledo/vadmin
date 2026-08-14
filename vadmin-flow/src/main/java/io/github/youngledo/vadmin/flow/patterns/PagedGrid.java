package io.github.youngledo.vadmin.flow.patterns;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.SortDirection;
import io.github.youngledo.vadmin.contracts.navigation.PagedQuery;
import io.github.youngledo.vadmin.contracts.navigation.PagedResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Binds a Grid and pager to one server-side page at a time using the shared paging contract. */
public final class PagedGrid<T> {
    @FunctionalInterface
    public interface PageLoader<T> {
        PagedResult<T> load(PagedQuery query);
    }

    private final Grid<T> grid;
    private final PageLoader<T> loader;
    private final Supplier<Map<String, String>> filters;
    private final String defaultSortField;
    private final PaginationBar paginationBar;
    private final Map<PagedQuery, PagedResult<T>> cache = new HashMap<>();
    private PagedResult<T> currentResult = new PagedResult<>(List.of(), 0);
    private int currentPage;
    private String sortField;
    private boolean ascending = true;

    public PagedGrid(Grid<T> grid, PageLoader<T> loader, String defaultSortField) {
        this(grid, loader, () -> Map.of(), defaultSortField);
    }

    public PagedGrid(Grid<T> grid, PageLoader<T> loader, Supplier<Map<String, String>> filters,
                     String defaultSortField) {
        this.grid = Objects.requireNonNull(grid);
        this.loader = Objects.requireNonNull(loader);
        this.filters = Objects.requireNonNull(filters);
        this.defaultSortField = Objects.requireNonNull(defaultSortField);
        this.sortField = defaultSortField;
        grid.setPageSize(50);
        grid.setEmptyStateText(grid.getTranslation("flow.grid.empty"));
        paginationBar = new PaginationBar(this::showPreviousPage, this::showNextPage);
        grid.addSortListener(event -> {
            var order = event.getSortOrder().stream().findFirst();
            sortField = order.map(item -> item.getSorted().getKey())
                    .filter(Objects::nonNull)
                    .orElse(this.defaultSortField);
            ascending = order.map(item -> item.getDirection() == SortDirection.ASCENDING).orElse(true);
            currentPage = 0;
            loadCurrentPage();
        });
        loadCurrentPage();
    }

    public PaginationBar getPaginationBar() {
        return paginationBar;
    }

    public List<T> getCurrentPageItems() {
        return currentResult.items();
    }

    /** Reloads the first server page after caller-owned filter or data changes. */
    public void refresh() {
        cache.clear();
        currentPage = 0;
        loadCurrentPage();
    }

    PagedResult<T> load(PagedQuery query) {
        return cache.computeIfAbsent(query, loader::load);
    }

    void loadCurrentPage() {
        currentResult = load(queryForCurrentPage());
        var pageCount = pageCount(currentResult.total());
        if (pageCount > 0 && currentPage >= pageCount) {
            currentPage = pageCount - 1;
            currentResult = load(queryForCurrentPage());
            pageCount = pageCount(currentResult.total());
        }
        grid.setItems(currentResult.items());
        paginationBar.setPage(currentPage, pageCount, currentResult.total());
    }

    private void showPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadCurrentPage();
        }
    }

    private void showNextPage() {
        if (currentPage + 1 < pageCount(currentResult.total())) {
            currentPage++;
            loadCurrentPage();
        }
    }

    private PagedQuery queryForCurrentPage() {
        return new PagedQuery(currentPage, grid.getPageSize(), sortField, ascending, filters.get());
    }

    private int pageCount(long total) {
        return Math.toIntExact((total + grid.getPageSize() - 1) / grid.getPageSize());
    }
}

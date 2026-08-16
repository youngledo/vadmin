package io.github.youngledo.vadmin.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.grid.Grid;
import io.github.youngledo.vadmin.contracts.navigation.PagedQuery;
import io.github.youngledo.vadmin.contracts.navigation.PagedResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PagedGridTest {
    @Test
    void cachesTheInitiallyLoadedQueryUntilTheGridIsRefreshed() {
        var loads = new AtomicInteger();
        var grid = new PagedGrid<>(new Grid<>(Row.class, false), query -> {
            loads.incrementAndGet();
            return new PagedResult<>(List.of(new Row("Ada")), 1);
        }, "name");
        var query = new PagedQuery(0, 20, "name", true, java.util.Map.of());

        grid.load(query);
        grid.load(query);
        grid.refresh();
        grid.load(query);

        assertThat(loads).hasValue(4);
    }

    @Test
    void loadsTheInitialServerPageWithFiltersFromTheSharedPagingContract() {
        var observed = new AtomicReference<PagedQuery>();
        var component = new Grid<Row>(Row.class, false);
        new PagedGrid<>(component, query -> {
            observed.set(query);
            return new PagedResult<>(List.of(new Row("Ada")), 1);
        }, () -> java.util.Map.of("q", "ada"), "name");

        assertThat(observed).hasValue(new PagedQuery(0, 50, "name", true, java.util.Map.of("q", "ada")));
    }

    @Test
    void exposesServerSidePageNavigationWithoutLoadingAllResults() {
        var observed = new ArrayList<PagedQuery>();
        var component = new Grid<Row>(Row.class, false);
        var pagedGrid = new PagedGrid<>(component, query -> {
            observed.add(query);
            var rows = rowsFor(query);
            return new PagedResult<>(rows, 120);
        }, "name");

        assertThat(observed).containsExactly(new PagedQuery(0, 50, "name", true, java.util.Map.of()));
        assertThat(pagedGrid.getPaginationBar().getSummary()).isEqualTo("Page 1 of 3, 120 results");
        assertThat(pagedGrid.getPaginationBar().getWidth()).isEqualTo("100%");
        assertThat(pagedGrid.getPaginationBar().getJustifyContentMode())
                .isEqualTo(com.vaadin.flow.component.orderedlayout.HorizontalLayout.JustifyContentMode.BETWEEN);
        assertThat(pagedGrid.getPaginationBar().getNavigation().getChildren())
                .contains(pagedGrid.getPaginationBar().getPreviousAction(), pagedGrid.getPaginationBar().getNextAction());
        assertThat(pagedGrid.getPaginationBar().getPreviousAction().isEnabled()).isFalse();
        assertThat(pagedGrid.getPaginationBar().getNextAction().isEnabled()).isTrue();

        pagedGrid.getPaginationBar().getNextAction().click();

        assertThat(observed).containsExactly(
                new PagedQuery(0, 50, "name", true, java.util.Map.of()),
                new PagedQuery(1, 50, "name", true, java.util.Map.of()));
        assertThat(pagedGrid.getPaginationBar().getSummary()).isEqualTo("Page 2 of 3, 120 results");
        assertThat(pagedGrid.getCurrentPageItems()).hasSize(50)
                .first().extracting(Row::name).isEqualTo("row-50");
    }

    @Test
    void hidesPagerForOnePageAndResetsCurrentPageWhenRefreshed() {
        var observed = new ArrayList<PagedQuery>();
        var component = new Grid<Row>(Row.class, false);
        var pagedGrid = new PagedGrid<>(component, query -> {
            observed.add(query);
            return new PagedResult<>(List.of(new Row("Ada")), 1);
        }, "name");

        assertThat(pagedGrid.getPaginationBar().isVisible()).isFalse();

        pagedGrid.refresh();

        assertThat(observed).containsExactly(
                new PagedQuery(0, 50, "name", true, java.util.Map.of()),
                new PagedQuery(0, 50, "name", true, java.util.Map.of()));
    }

    private static List<Row> rowsFor(PagedQuery query) {
        var start = query.page() * query.pageSize();
        var end = Math.min(start + query.pageSize(), 120);
        return java.util.stream.IntStream.range(start, end)
                .mapToObj(index -> new Row("row-" + index))
                .toList();
    }

    private record Row(String name) { }
}

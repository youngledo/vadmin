package io.github.vaadinadminstarter.flow.patterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import io.github.vaadinadminstarter.contracts.navigation.PagedQuery;
import io.github.vaadinadminstarter.contracts.navigation.PagedResult;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PagedGridTest {
    @Test
    void cachesAQueryUntilTheGridIsRefreshed() {
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

        assertThat(loads).hasValue(2);
    }

    @Test
    void mapsGridPageSortAndFiltersToTheSharedPagingContract() {
        var observed = new AtomicReference<PagedQuery>();
        var component = new Grid<Row>(Row.class, false);
        new PagedGrid<>(component, query -> {
            observed.set(query);
            return new PagedResult<>(List.of(new Row("Ada")), 1);
        }, () -> java.util.Map.of("q", "ada"), "name");

        @SuppressWarnings("unchecked")
        var provider = (DataProvider<Row, Void>) component.getDataProvider();
        provider.fetch(new Query<Row, Void>(50, 50,
                List.of(new QuerySortOrder("email", SortDirection.DESCENDING)), null, null)).toList();

        assertThat(observed).hasValue(new PagedQuery(1, 50, "email", false, java.util.Map.of("q", "ada")));
    }

    private record Row(String name) { }
}

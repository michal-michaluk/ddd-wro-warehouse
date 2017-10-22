package warehouse.products;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetIterable;
import org.sql2o.Sql2o;
import warehouse.PaletteLabel;
import warehouse.Persistence;
import warehouse.quality.Destroyed;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by michal on 21.10.2017.
 */
@AllArgsConstructor
public class ProductStockSql2oEventsStore implements ProductStockEventStore {

    private final Sql2o sql2o;

    private final Predicate<Object> archiveUnit =
            event -> event instanceof Destroyed ||
                    event instanceof Delivered;

    @Data
    private static class ProductStockHistoryEvent {
        private final long id;
        private final Instant created;
        private final String refNo;
        private final String unit;
        private final String type;
        private final String content;
        private final boolean inStock;
    }

    @Override
    public EventId persist(PaletteLabel storageUnit, Object event) {
        String json = Persistence.serialization.serialize(event);
        String alias = Persistence.serialization.of(event.getClass()).getAlias();

        try (Connection connection = sql2o.beginTransaction()) {
            Long eventId = connection.createQuery(
                    "insert into warehouse.ProductStockHistory(refNo, unit, type, content) " +
                            "values (:refNo, :unit, :type, cast(:content AS json))", true)
                    .addParameter("refNo", storageUnit.getRefNo())
                    .addParameter("unit", storageUnit.getId())
                    .addParameter("type", alias)
                    .addParameter("content", json)
                    .executeUpdate()
                    .getKey(Long.class);

            if (archiveUnit.test(event)) {
                connection.createQuery("update warehouse.ProductStockHistory set inStock = false where unit = :unit")
                        .addParameter("unit", storageUnit.getId())
                        .executeUpdate();
            }
            connection.commit();
            return new EventId(eventId);
        }
    }

    @Override
    public List<Object> readEventsInStock(String refNo) {
        try (Connection connection = sql2o.open();
             Query query = connection.createQuery(
                     "select * from warehouse.ProductStockHistory where refNo = :refNo and inStock = true order by id");
             ResultSetIterable<ProductStockHistoryEvent> result = query
                     .addParameter("refNo", refNo)
                     .executeAndFetchLazy(ProductStockHistoryEvent.class)) {
            return deserialize(result);
        }
    }

    @Override
    public List<Object> readEventsSince(String refNo, EventId since) {
        try (Connection connection = sql2o.open();
             Query query = connection.createQuery(
                     "select * from warehouse.ProductStockHistory where refNo = :refNo and id > :since order by id");
             ResultSetIterable<ProductStockHistoryEvent> result = query
                     .addParameter("refNo", refNo)
                     .addParameter("since", since.getId())
                     .executeAndFetchLazy(ProductStockHistoryEvent.class)) {
            return deserialize(result);
        }
    }

    private List<Object> deserialize(ResultSetIterable<ProductStockHistoryEvent> result) {
        return StreamSupport.stream(result.spliterator(), false)
                .map(entry -> Persistence.serialization.deserialize(entry.getContent(), entry.getType()))
                .collect(Collectors.toList());
    }
}

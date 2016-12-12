package warehouse.products;

import lombok.Data;
import org.sql2o.ResultSetIterable;
import org.sql2o.Sql2o;
import tools.MultiMethod;
import warehouse.EventMappings;
import warehouse.Persistence;
import warehouse.locations.BasicLocationPicker;

import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by michal on 13.07.2016.
 */
public class ProductStockDatabaseRepository implements ProductStockExtendedRepository {

    @Data
    private static class HistoryEvent {
        private final long id;
        private final LocalDateTime created;
        private final String refNo;
        private final String type;
        private final String content;
    }

    private static final MultiMethod<ProductStock, Void> productStock$handle =
            MultiMethod.in(ProductStock.class).method("handle")
                    .lookup(MethodHandles.lookup())
                    .onMissingHandler(Exception::printStackTrace);

    // caches
    private final Map<String, ProductStock> products = new ConcurrentHashMap<>();

    // repository dependencies
    private final Sql2o sql2o;

    // aggregate dependencies
    private final PaletteValidator validator;
    private final BasicLocationPicker locationPicker;
    private final ProductStock.Events events;
    private final Clock clock;

    public ProductStockDatabaseRepository(EventMappings mappings, Sql2o sql2o) {
        this.sql2o = sql2o;
        this.validator = new PaletteValidator();
        this.locationPicker = new BasicLocationPicker(Collections.emptyMap());
        this.events = new ProductStockEventsHandler(this, mappings.new ProductStocks());
        this.clock = Clock.systemDefaultZone();
    }

    @Override
    public Optional<ProductStock> get(String refNo) {
        if (products.containsKey(refNo)) {
            return Optional.of(products.get(refNo));
        } else {
            List<Object> history = retrieve(refNo);
            //List<ProductStock.PaletteInformation> ormEntities = retrieve(refNo);
            ProductStock stock = new ProductStock(refNo, validator, locationPicker, events, clock);
            products.put(refNo, stock);
            for (Object event : history) {
                try {
                    productStock$handle.call(stock, event);
                } catch (Throwable throwable) {
                    // stock <refNo> cannot replay event <event> cause <throwable>
                    throwable.printStackTrace();
                }
            }
            return Optional.of(stock);
        }
    }

    public List<Object> readEvents(String refNo) {
        return retrieve(refNo);
    }

    @Override
    public void persist(String refNo, Object event) {
        String json = Persistence.serialization.serialize(event);
        String alias = Persistence.serialization.of(event.getClass()).getAlias();

        try (org.sql2o.Connection connection = sql2o.beginTransaction()) {
            connection.createQuery(
                    "insert into warehouse.ProductStockHistory(refNo, type, content) " +
                            "values (:refNo, :type, cast(:content AS json))")
                    .addParameter("refNo", refNo)
                    .addParameter("type", alias)
                    .addParameter("content", json)
                    .executeUpdate();
            connection.commit();
        }
    }

    protected List<Object> retrieve(String refNo) {
        try (ResultSetIterable<HistoryEvent> result = sql2o.open().createQuery(
                "select * from warehouse.ProductStockHistory where refNo = :refNo order by id")
                .addParameter("refNo", refNo)
                .executeAndFetchLazy(HistoryEvent.class)) {
            return StreamSupport.stream(result.spliterator(), false)
                    .map(entry -> Persistence.serialization.deserialize(entry.getContent(), entry.getType()))
                    .collect(Collectors.toList());
        }
    }
}

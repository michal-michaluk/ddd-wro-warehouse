package warehouse.products;

import lombok.Data;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetIterable;
import org.sql2o.Sql2o;
import tools.AgentQueue;
import tools.EventsApplier;
import warehouse.EventMappings;
import warehouse.OpsSupport;
import warehouse.PaletteLabel;
import warehouse.Persistence;
import warehouse.locations.BasicLocationPicker;
import warehouse.quality.Locked;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by michal on 13.07.2016.
 */
public class ProductStockDatabaseRepository implements ProductStockExtendedRepository {

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

    private static final EventsApplier<ProductStock> applier =
            EventsApplier.forType(ProductStock.class)
                    .define(Registered.class, ProductStock::apply)
                    .define(Stored.class, ProductStock::apply)
                    .define(Picked.class, ProductStock::apply)
                    .define(Locked.class, ProductStock::apply)
                    .onMissingDefinitionSkip();

    // caches
    private final Map<String, ProductStockAgent> products = new ConcurrentHashMap<>();

    // repository dependencies
    private final Sql2o sql2o;
    private final OpsSupport support;

    // aggregate dependencies
    private final PaletteValidator validator;
    private final BasicLocationPicker locationPicker;
    private final ProductStock.EventsContract events;
    private final Clock clock;

    public ProductStockDatabaseRepository(EventMappings mappings, Sql2o sql2o, OpsSupport support) {
        this.sql2o = sql2o;
        this.support = support;
        this.validator = new PaletteValidator();
        this.locationPicker = new BasicLocationPicker(Collections.emptyMap());
        this.events = new ProductStockEventsHandler(this, mappings.productStocks());
        this.clock = Clock.systemDefaultZone();
    }

    @Override
    public Optional<ProductStockAgent> get(String refNo) {
        return Optional.of(products.computeIfAbsent(refNo, id -> {
            List<Object> history = retrieve(id);
            if (history.isEmpty()) {
                support.initialisingStockForNewProduct(id, validator, locationPicker);
            }
            ProductStock stock = new ProductStock(id, validator, locationPicker, events, clock);
            applier.apply(stock, history);
            return new ProductStockAgent(id, stock, new AgentQueue());
        }));
    }

    public List<Object> readEvents(String refNo) {
        return retrieve(refNo);
    }

    @Override
    public void persist(PaletteLabel storageUnit, Object event) {
        String json = Persistence.serialization.serialize(event);
        String alias = Persistence.serialization.of(event.getClass()).getAlias();

        try (Connection connection = sql2o.beginTransaction()) {
            connection.createQuery(
                    "insert into warehouse.ProductStockHistory(refNo, unit, type, content) " +
                            "values (:refNo, :unit, :type, cast(:content AS json))")
                    .addParameter("refNo", storageUnit.getRefNo())
                    .addParameter("unit", storageUnit.getId())
                    .addParameter("type", alias)
                    .addParameter("content", json)
                    .executeUpdate();
            connection.commit();
        }
    }

    protected List<Object> retrieve(String refNo) {
        try (Connection connection = sql2o.open();
             Query query = connection.createQuery(
                     "select * from warehouse.ProductStockHistory where refNo = :refNo order by id");
             ResultSetIterable<ProductStockHistoryEvent> result = query
                     .addParameter("refNo", refNo)
                     .executeAndFetchLazy(ProductStockHistoryEvent.class)) {
            return StreamSupport.stream(result.spliterator(), false)
                    .map(entry -> Persistence.serialization.deserialize(entry.getContent(), entry.getType()))
                    .collect(Collectors.toList());
        }
    }
}

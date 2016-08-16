package warehouse.products;

import lombok.Data;
import tools.FileStore;
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

/**
 * Created by michal on 13.07.2016.
 */
public class ProductStockFileRepository implements ProductStockRepository {

    @Data
    public static class EventEntry {
        private final UUID id;
        private final LocalDateTime created;
        private final String refNo;
        private final String type;
        private final String json;
    }

    private static final MultiMethod<ProductStock, Void> productStock$handle =
            MultiMethod.in(ProductStock.class).method(void.class, "handle")
                    .lookup(MethodHandles.lookup())
                    .onMissingHandler(Exception::printStackTrace);

    // caches
    private final Map<String, ProductStock> products = new ConcurrentHashMap<>();

    // repository dependencies
    private final FileStore store = new FileStore();

    // aggregate dependencies
    private final PaletteValidator validator;
    private final BasicLocationPicker locationPicker;
    private final ProductStock.Events events;
    private final Clock clock;

    public ProductStockFileRepository(EventMappings mappings) {
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
            ProductStock stock = new ProductStock(refNo, validator, locationPicker, events, clock);
            products.put(refNo, stock);
            for (Object event : history) {
                try {
                    productStock$handle.call(stock, event);
                } catch (Throwable throwable) {
                    // for stock <refNo> cannot reply event <event> cause <throwable>
                    throwable.printStackTrace();
                }
            }
            return Optional.of(stock);
        }
    }

    public List<Object> readEvents(String refNo) {
        return retrieve(refNo);
    }

    protected void persist(String refNo, Object event) {
        String json = Persistence.serialisation.serialize(event);
        String alias = Persistence.serialisation.of(event.getClass()).getAlias();
        EventEntry entry = new ProductStockFileRepository.EventEntry(
                UUID.randomUUID(), LocalDateTime.now(), refNo, alias, json);
        store.append(refNo, entry);
    }

    protected List<Object> retrieve(String refNo) {
        List<ProductStockFileRepository.EventEntry> history = store.readAll(refNo, EventEntry.class);
        return history.stream()
                .map(entry -> Persistence.serialisation.deserialize(entry.getJson(), entry.getType()))
                .collect(Collectors.toList());
    }
}

package warehouse.products;

import tools.AgentQueue;
import tools.EventsApplier;
import warehouse.EventMappings;
import warehouse.OpsSupport;
import warehouse.carts.CartDefinitionRepository;
import warehouse.locations.BasicLocationPicker;
import warehouse.quality.Destroyed;
import warehouse.quality.Locked;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by michal on 13.07.2016.
 */
public class ProductStockEventSourcingRepository implements ProductStockAgentRepository {

    private static final EventsApplier<ProductStock> applier =
            EventsApplier.forType(ProductStock.class)
                    .define(Registered.class, ProductStock::apply)
                    .define(Stored.class, ProductStock::apply)
                    .define(Picked.class, ProductStock::apply)
                    .define(Locked.class, ProductStock::apply)
                    .define(Delivered.class, ProductStock::delivered)
                    .define(Destroyed.class, ProductStock::destroyed)
                    .onMissingDefinitionSkip();

    // caches
    private final Map<String, ProductStockAgent> products = new ConcurrentHashMap<>();

    // repository dependencies
    private final ProductStockEventStore eventStore;
    private final OpsSupport support;

    // aggregate dependencies
    private final BasicLocationPicker locationPicker;
    private final ProductStock.EventsContract events;
    private final Clock clock;
    private final CartDefinitionRepository cartDefinitions;

    public ProductStockEventSourcingRepository(EventMappings mappings, ProductStockEventStore eventStore, OpsSupport support, CartDefinitionRepository cartDefinitions) {
        this.eventStore = eventStore;
        this.support = support;
        this.cartDefinitions = cartDefinitions;
        this.locationPicker = new BasicLocationPicker(Collections.emptyMap());
        this.events = mappings.productStocks();
        this.clock = Clock.systemDefaultZone();
    }

    @Override
    public Optional<ProductStockAgent> get(String refNo) {
        return Optional.of(products.computeIfAbsent(refNo, id -> {
            List<Object> history = eventStore.readEventsInStock(id);
            if (history.isEmpty()) {
                support.initialisingStockForNewProduct(id, locationPicker);
            }
            ProductStockEventsHandler handler = new ProductStockEventsHandler(events);
            StorageUnitValidator validator = pickValidator(refNo);
            ProductStock stock = new ProductStock(id, locationPicker, validator, handler, clock);
            applier.apply(stock, history);
            return new ProductStockAgent(id, stock, handler, new AgentQueue());
        }));
    }

    private StorageUnitValidator pickValidator(String refNo) {
        return cartDefinitions.getValidator(refNo)
                .map(StorageUnitValidator.class::cast)
                .orElseGet(PaletteValidator::new);
    }

    class ProductStockEventsHandler implements ProductStock.EventsContract {

        private final ProductStock.EventsContract delegate;
        private List<Object> last;

        ProductStockEventsHandler(ProductStock.EventsContract delegate) {
            this.delegate = delegate;
            last = new LinkedList<>();
        }

        @Override
        public void emit(Registered event) {
            eventStore.persist(event.getPaletteLabel(), event);
            last.add(event);
            delegate.emit(event);
            // emit outside app
        }

        @Override
        public void emit(Stored event) {
            eventStore.persist(event.getPaletteLabel(), event);
            last.add(event);
            delegate.emit(event);
            // emit outside app
        }

        @Override
        public void emit(Picked event) {
            eventStore.persist(event.getPaletteLabel(), event);
            last.add(event);
            delegate.emit(event);
            // emit outside app
        }

        @Override
        public void emit(Locked event) {
            eventStore.persist(event.getPaletteLabel(), event);
            last.add(event);
            delegate.emit(event);
            // emit outside app
        }

        List<Object> getLast() {
            List<Object> events = this.last;
            this.last = new LinkedList<>();
            return Collections.unmodifiableList(events);
        }
    }
}

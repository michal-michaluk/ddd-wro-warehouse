package warehouse.picklist;

import tools.MultiMethod;
import warehouse.locations.Location;
import warehouse.products.ProductStockFileRepository;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Querying and influencing events behaviour:
 * lot of stored products without any activity
 * queried once a day for multiple products
 * lot of ReadyToStore and Delivered during day for selected products
 * queried multiple times over the day
 * <p>
 * technical chooses:
 * view exists temporally lookup memory for subset of products
 * products loaded on demand reading all stock history (no view snapshot for now)
 * when products is lookup memory, view listens on events
 * when products not lookup memory, repo filters out events
 * memory content cleared once a day
 * <p>
 * Created by michal on 13.07.2016.
 */
public class FifoViewRepository implements FifoRepository {

    private static final MultiMethod<Fifo.PerProduct, Void> perProduct$handle = MultiMethod
            .in(Fifo.PerProduct.class).method("handle")
            .lookup(MethodHandles.lookup())
            .onMissingHandler(MultiMethod.IGNORE);

    // caches
    private final Fifo fifo;
    private final Map<String, Fifo.PerProduct> products = new ConcurrentHashMap<>();

    // aggregate dependencies
    private final Fifo.PaletteLocations paletteLocations;

    // repository dependencies
    private final ProductStockFileRepository stocks;

    public FifoViewRepository(ProductStockFileRepository stocks) {
        this.stocks = stocks;
        this.paletteLocations = paletteLabel ->
                stocks.get(paletteLabel.getRefNo())
                        .map(productStock -> productStock.getLocation(paletteLabel))
                        .orElse(Location.unknown());
        this.fifo = new Fifo(paletteLocations, refNo ->
                products.computeIfAbsent(refNo, this::load)
        );
    }

    @Override
    public Fifo get() {
        return fifo;
    }

    @Override
    public void handle(String refNo, Object event) {
        if (products.containsKey(refNo)) {
            Fifo.PerProduct product = products.get(refNo);
            try {
                perProduct$handle.call(product, event);
            } catch (Throwable throwable) {
                // for fifo <refNo> cannot reply event <event> cause <throwable>
                throwable.printStackTrace();
            }
        }
    }

    public void clear() {
        products.clear();
    }

    private Fifo.PerProduct load(String refNo) {
        List<Object> events = stocks.readEvents(refNo);
        Fifo.PerProduct product = new Fifo.PerProduct();
        for (Object event : events) {
            try {
                perProduct$handle.call(product, event);
            } catch (Throwable throwable) {
                // for fifo <refNo> cannot reply event <event> cause <throwable>
                throwable.printStackTrace();
            }
        }
        return product;
    }
}

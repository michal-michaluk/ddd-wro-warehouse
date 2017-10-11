package warehouse.picklist;

import tools.MultiMethod;
import warehouse.locations.Location;
import warehouse.products.ProductStockExtendedRepository;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages in memory projection of fifo ordered picklists.
 * Domain charatceristics of fifo ordered picklists view:
 * <ul>
 * <li>there is a lot of stored products without any activity, neither query nor update</li>
 * <li>products scheduled today for delivery (limited subset of products) will be queried multiple times</li>
 * <li>products produced/stored today will update view content but don't need to be queried</li>
 * </ul>
 * technical chooses:
 * <ul>
 * <li>view exists temporally in memory for subset of products (delivered/queried today)</li>
 * <li>when products is loaded on demand, product stock history is loaded (no view snapshot for now)</li>
 * <li>when products is already in memory, view listens on events and keeps memory state up to date</li>
 * <li>when products is not loaded in memory, repo ignores events</li>
 * <li>memory content is cleared once a day</li>
 * </ul>
 * Created by michal on 13.07.2016.
 */
public class FifoViewProjection implements FifoRepository {

    private static final MultiMethod<Fifo.PerProduct, Void> perProduct$handle = MultiMethod
            .in(Fifo.PerProduct.class).method("apply")
            .lookup(MethodHandles.lookup())
            .onMissingHandler(MultiMethod.IGNORE);

    // caches
    private final Fifo fifo;
    private final Map<String, Fifo.PerProduct> products = new ConcurrentHashMap<>();

    // aggregate dependencies
    private final Fifo.PaletteLocations paletteLocations;

    // repository dependencies
    private final ProductStockExtendedRepository stocks;

    public FifoViewProjection(ProductStockExtendedRepository stocks) {
        this.stocks = stocks;
        this.paletteLocations = paletteLabel ->
                stocks.get(paletteLabel.getRefNo())
                        .map(productStock -> productStock.getLocationSync(paletteLabel))
                        .orElse(Location.unknown());
        Fifo.Products products = refNo ->
                this.products.computeIfAbsent(refNo, this::load);
        this.fifo = new Fifo(paletteLocations, products);
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

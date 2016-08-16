package warehouse;

import warehouse.locations.BasicLocationPicker;
import warehouse.picklist.Fifo;
import warehouse.products.PaletteValidator;
import warehouse.products.ProductStock;

import java.time.Clock;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by michal on 13.07.2016.
 */
public class Repository {

    private final Fifo fifo = new Fifo(null, null);
    private final ProductStock.Events events;

    public Repository(EventMappings mappings) {
        this.events = mappings.new ProductStocks();
    }

    public Optional<ProductStock> get(String refNo) {
        return fake(refNo);
    }

    public Fifo getFifo() {
        return fifo;
    }

    private Optional<ProductStock> fake(String refNo) {
        return Optional.of(new ProductStock(refNo,
                new PaletteValidator(), new BasicLocationPicker(Collections.emptyMap()),
                events, Clock.systemDefaultZone())
        );
    }
}

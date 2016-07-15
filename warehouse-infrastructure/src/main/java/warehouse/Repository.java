package warehouse;

import warehouse.products.ProductStock;

import java.time.Clock;
import java.util.Optional;

/**
 * Created by michal on 13.07.2016.
 */
public class Repository {
    public Optional<ProductStock> get(String refNo) {
        return Optional.of(new ProductStock(
                refNo, new EventsHandling(), null, Clock.systemDefaultZone())
        );
    }
}

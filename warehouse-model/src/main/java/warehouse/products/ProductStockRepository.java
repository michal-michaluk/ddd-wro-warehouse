package warehouse.products;

import java.util.Optional;

/**
 * Created by michal on 14.08.2016.
 */
public interface ProductStockRepository {
    Optional<ProductStock> get(String refNo);
}

package warehouse.products;

import java.util.List;

/**
 * Created by michal on 12.12.2016.
 */
public interface ProductStockExtendedRepository extends ProductStockAgentRepository {
    void persist(String refNo, Object event);

    List<Object> readEvents(String refNo);
}

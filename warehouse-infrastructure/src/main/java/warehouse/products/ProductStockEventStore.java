package warehouse.products;

import lombok.ToString;
import lombok.Value;
import warehouse.PaletteLabel;

import java.util.List;

/**
 * Created by michal on 12.12.2016.
 */
public interface ProductStockEventStore {
    EventId persist(PaletteLabel storageUnit, Object event);

    List<Object> readEventsInStock(String refNo);
    List<Object> readEventsSince(String refNo, EventId since);

    @Value
    @ToString(of = "id")
    class EventId {
        Object id;
    }
}


package warehouse.picklist;

import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by michal on 15.07.2016.
 */
@Value
@Builder
public class Order {

    private final List<Item> items;

    private Order(List<Item> items) {
        this.items = Collections.unmodifiableList(items);
    }

    @Value
    public static class Item {
        private final String refNo;
        private final int amount;
    }

    public static class OrderBuilder {
        public OrderBuilder add(String refNo, int amount) {
            items.add(new Item(refNo, amount));
            return this;
        }

        private OrderBuilder() {
            items = new LinkedList<>();
        }
    }
}

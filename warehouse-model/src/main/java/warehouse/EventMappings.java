package warehouse;

import lombok.AllArgsConstructor;
import warehouse.picklist.Fifo;
import warehouse.products.NewPaletteReadyToStore;
import warehouse.products.Picked;
import warehouse.products.ProductStock;
import warehouse.products.Stored;

/**
 * Created by michal on 16.07.2016.
 */
public class EventMappings {

    @AllArgsConstructor
    public class ProductStocks implements ProductStock.Events {

        private final Fifo fifo;

        @Override
        public void fire(NewPaletteReadyToStore event) {
            fifo.handle(event);
        }

        @Override
        public void fire(Stored event) {
            fifo.handle(event);
        }

        @Override
        public void fire(Picked event) {

        }
    }
}

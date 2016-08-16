package warehouse;

import lombok.AllArgsConstructor;
import warehouse.picklist.FifoRepository;
import warehouse.products.*;
import warehouse.quality.Destroyed;
import warehouse.quality.Locked;
import warehouse.quality.Unlocked;

/**
 * Created by michal on 16.07.2016.
 */
public class EventMappings {

    private ProductStockRepository stocks;
    private FifoRepository fifo;

    @AllArgsConstructor
    public class RemoteEvents {

        public void emit(Locked event) {
            fifo.handle(event.getPaletteLabel().getRefNo(), event);
        }

        public void emit(Unlocked event) {
            fifo.handle(event.getPaletteLabel().getRefNo(), event);
        }

        public void emit(Destroyed event) {
            fifo.handle(event.getPaletteLabel().getRefNo(), event);
        }

        public void emit(Delivered event) {
            stocks.get(event.getPaletteLabel().getRefNo())
                    .ifPresent(productStock -> productStock.delivered(event));
            fifo.handle(event.getPaletteLabel().getRefNo(), event);
        }
    }

    @AllArgsConstructor
    public class ProductStocks implements ProductStock.Events {

        @Override
        public void emit(ReadyToStore event) {
            fifo.handle(event.getPaletteLabel().getRefNo(), event);
        }

        @Override
        public void emit(Stored event) {
        }

        @Override
        public void emit(Picked event) {
        }

        @Override
        public void emit(Locked event) {
            fifo.handle(event.getPaletteLabel().getRefNo(), event);
        }
    }

    protected void dependencies(ProductStockRepository stocks, FifoRepository fifo) {
        this.stocks = stocks;
        this.fifo = fifo;
    }
}

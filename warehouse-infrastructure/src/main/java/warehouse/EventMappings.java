package warehouse;

import lombok.AllArgsConstructor;
import warehouse.picklist.FifoRepository;
import warehouse.products.*;
import warehouse.quality.Destroyed;
import warehouse.quality.Locked;
import warehouse.quality.Unlocked;

import java.util.concurrent.CompletableFuture;

/**
 * Created by michal on 16.07.2016.
 */
public class EventMappings {

    private ProductStockAgentRepository stocks;
    private FifoRepository fifo;
    private OpsSupport support;

    public ExternalEvents externalEvents() {
        return new ExternalEvents();
    }

    public ProductStock.EventsContract productStocks() {
        return new ProductStocks();
    }

    @AllArgsConstructor
    public class ExternalEvents {

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
                    .ifPresent(productStock -> productStock.delivered(event)
                            .handle((object, throwable) -> support.appliedExternalEventOnProductStock(productStock, event, throwable))
                    );
            fifo.handle(event.getPaletteLabel().getRefNo(), event);
        }
    }

    @AllArgsConstructor
    private class ProductStocks implements ProductStock.EventsContract {

        @Override
        public void emit(Registered event) {
            async(() -> fifo.handle(event.getPaletteLabel().getRefNo(), event));
        }

        @Override
        public void emit(Stored event) {
        }

        @Override
        public void emit(Picked event) {
        }

        @Override
        public void emit(Locked event) {
            async(() -> fifo.handle(event.getPaletteLabel().getRefNo(), event));
        }
    }

    void dependencies(ProductStockAgentRepository stocks, FifoRepository fifo, OpsSupport support) {
        this.stocks = stocks;
        this.fifo = fifo;
        this.support = support;
    }

    void async(Runnable handler) {
        CompletableFuture.runAsync(handler);
    }
}

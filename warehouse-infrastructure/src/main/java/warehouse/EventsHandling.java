package warehouse;

import warehouse.products.Picked;
import warehouse.products.ProductStock;
import warehouse.products.ReadyToStore;
import warehouse.products.Stored;
import warehouse.quality.Locked;

/**
 * Created by michal on 08.06.2016.
 */
public class EventsHandling implements ProductStock.Events {

    private final ProductStock.Events delegate;

    public EventsHandling(ProductStock.Events delegate) {
        this.delegate = delegate;
    }

    @Override
    public void emit(ReadyToStore event) {
        // persist event
        delegate.emit(event);
        // emit outside app
    }

    @Override
    public void emit(Stored event) {
        // persist event
        delegate.emit(event);
        // emit outside app
    }

    @Override
    public void emit(Picked event) {
        // persist event
        delegate.emit(event);
        // emit outside app
    }

    @Override
    public void emit(Locked event) {
        // persist event
        delegate.emit(event);
        // emit outside app
    }
}

package warehouse;

import warehouse.products.ReadyToStore;
import warehouse.products.Picked;
import warehouse.products.ProductStock;
import warehouse.products.Stored;

/**
 * Created by michal on 08.06.2016.
 */
public class EventsHandling implements ProductStock.Events {

    private final ProductStock.Events delegate;

    public EventsHandling(ProductStock.Events delegate) {
        this.delegate = delegate;
    }

    @Override
    public void fire(ReadyToStore event) {
        // persist event
        delegate.fire(event);
        // emit outside app
    }

    @Override
    public void fire(Stored event) {
        // persist event
        delegate.fire(event);
        // emit outside app
    }

    @Override
    public void fire(Picked event) {
        // persist event
        delegate.fire(event);
        // emit outside app
    }
}

package warehouse.products;

import warehouse.quality.Locked;

/**
 * Created by michal on 08.06.2016.
 */
public class ProductStockEventsHandler implements ProductStock.EventsContract {

    private final ProductStockExtendedRepository repository;
    private final ProductStock.EventsContract delegate;

    public ProductStockEventsHandler(ProductStockExtendedRepository repository, ProductStock.EventsContract delegate) {
        this.repository = repository;
        this.delegate = delegate;
    }

    @Override
    public void emit(Registered event) {
        repository.persist(event.getPaletteLabel(), event);
        delegate.emit(event);
        // emit outside app
    }

    @Override
    public void emit(Stored event) {
        repository.persist(event.getPaletteLabel(), event);
        delegate.emit(event);
        // emit outside app
    }

    @Override
    public void emit(Picked event) {
        repository.persist(event.getPaletteLabel(), event);
        delegate.emit(event);
        // emit outside app
    }

    @Override
    public void emit(Locked event) {
        repository.persist(event.getPaletteLabel(), event);
        delegate.emit(event);
        // emit outside app
    }

}

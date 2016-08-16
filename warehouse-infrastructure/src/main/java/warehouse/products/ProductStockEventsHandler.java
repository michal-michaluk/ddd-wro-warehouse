package warehouse.products;

import warehouse.quality.Locked;

/**
 * Created by michal on 08.06.2016.
 */
public class ProductStockEventsHandler implements ProductStock.Events {

    private final ProductStockFileRepository repository;
    private final ProductStock.Events delegate;

    public ProductStockEventsHandler(ProductStockFileRepository repository, ProductStock.Events delegate) {
        this.repository = repository;
        this.delegate = delegate;
    }

    @Override
    public void emit(ReadyToStore event) {
        repository.persist(event.getPaletteLabel().getRefNo(), event);
        delegate.emit(event);
        // emit outside app
    }

    @Override
    public void emit(Stored event) {
        repository.persist(event.getPaletteLabel().getRefNo(), event);
        delegate.emit(event);
        // emit outside app
    }

    @Override
    public void emit(Picked event) {
        repository.persist(event.getPaletteLabel().getRefNo(), event);
        delegate.emit(event);
        // emit outside app
    }

    @Override
    public void emit(Locked event) {
        repository.persist(event.getPaletteLabel().getRefNo(), event);
        delegate.emit(event);
        // emit outside app
    }
}

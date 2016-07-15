package warehouse;

import warehouse.products.NewPaletteReadyToStore;
import warehouse.products.Picked;
import warehouse.products.ProductStock;
import warehouse.products.Stored;

/**
 * Created by michal on 08.06.2016.
 */
public class EventsHandling implements ProductStock.Events {

    @Override
    public void fire(NewPaletteReadyToStore newPaletteReadyToStore) {

    }

    @Override
    public void fire(Stored event) {

    }

    @Override
    public void fire(Picked event) {

    }
}

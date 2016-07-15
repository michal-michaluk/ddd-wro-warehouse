package warehouse.products;

import lombok.Data;
import warehouse.PaletteLabel;
import warehouse.locations.Location;
import warehouse.locations.PreferredLocationPicker;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by michal on 08.06.2016.
 */
public class ProductStock {

    private String refNo;
    private Events events;
    private PreferredLocationPicker locationsPicker;
    private Clock clock;

    private Map<PaletteLabel, PaletteInformation> stock = new HashMap<>();

    public ProductStock(String refNo, Events events, PreferredLocationPicker locationsPicker, Clock clock) {
        this.refNo = refNo;
        this.events = events;
        this.locationsPicker = locationsPicker;
        this.clock = clock;
    }

    public void completeNewPalette(CompleteNewPalette completeNewPalette) {
        assert refNo.equals(completeNewPalette.getPaletteLabel().getRefNo());
        // sprawdzamy czy zeskanowane boxy sa z zgodne z produktem na etykiecie palety
        // sprawdzamy czy wszystkie zeskanowane boxy sa tego samego typu

        NewPaletteReadyToStore event = new NewPaletteReadyToStore(
                completeNewPalette.getPaletteLabel(),
                LocalDateTime.now(clock),
                locationsPicker.suggestLocationFor(completeNewPalette.getPaletteLabel())
        );
        handle(event);
        events.fire(event);
    }

    public void pick(Pick pick) {
        assert refNo.equals(pick.getPaletteLabel().getRefNo());
        Location fromLocation = stock.get(pick.getPaletteLabel())
                .stored.map(Stored::getLocation).orElse(Location.production());
        Picked event = new Picked(pick.getPaletteLabel(), pick.getUser(), fromLocation);
        handle(event);
        events.fire(event);
    }

    public void store(Store store) {
        assert refNo.equals(store.getPaletteLabel().getRefNo());
        Stored event = new Stored(
                store.getPaletteLabel(),
                store.getLocation());
        handle(event);
        events.fire(event);
    }

    protected void handle(NewPaletteReadyToStore event) {
        stock.put(event.getLabel(), new PaletteInformation(event));
    }

    protected void handle(Picked event) {
        stock.get(event.getPaletteLabel()).picked(event);
    }

    protected void handle(Stored event) {
        stock.get(event.getPaletteLabel()).stored(event);
    }

    public interface Events {
        void fire(NewPaletteReadyToStore event);

        void fire(Stored event);

        void fire(Picked event);
    }

    @Data
    private class PaletteInformation {
        private final NewPaletteReadyToStore init;
        private Optional<Stored> stored;
        private Optional<Picked> picked;

        public void picked(Picked event) {
            picked = Optional.of(event);
        }

        public void stored(Stored event) {
            stored = Optional.of(event);
        }
    }
}

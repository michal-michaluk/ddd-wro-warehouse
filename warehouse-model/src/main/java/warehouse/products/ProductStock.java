package warehouse.products;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ProductStock {

    private String refNo;
    private PaletteValidator validator;
    private PreferredLocationPicker locationsPicker;
    private Events events;
    private Clock clock;

    private final Map<PaletteLabel, PaletteInformation> stock = new HashMap<>();

    public void registerNew(RegisterNew registerNew) {
        assert refNo.equals(registerNew.getPaletteLabel().getRefNo());
        validator.validate(registerNew);

        ReadyToStore event = new ReadyToStore(
                registerNew.getPaletteLabel(),
                LocalDateTime.now(clock),
                locationsPicker.suggestLocationFor(registerNew.getPaletteLabel())
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

    protected void handle(ReadyToStore event) {
        stock.put(event.getPaletteLabel(), new PaletteInformation(event));
    }

    protected void handle(Picked event) {
        stock.get(event.getPaletteLabel()).picked(event);
    }

    protected void handle(Stored event) {
        stock.get(event.getPaletteLabel()).stored(event);
    }

    public interface Events {
        void fire(ReadyToStore event);

        void fire(Stored event);

        void fire(Picked event);
    }

    @Data
    private class PaletteInformation {
        private final ReadyToStore init;
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

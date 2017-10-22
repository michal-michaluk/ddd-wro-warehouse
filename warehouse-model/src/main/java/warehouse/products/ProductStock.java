package warehouse.products;

import lombok.AllArgsConstructor;
import lombok.Data;
import warehouse.BoxLabel;
import warehouse.PaletteLabel;
import warehouse.locations.Location;
import warehouse.locations.PreferredLocationPicker;
import warehouse.quality.Destroyed;
import warehouse.quality.Locked;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by michal on 08.06.2016.
 */
@AllArgsConstructor
public class ProductStock {

    private String refNo;
    private PreferredLocationPicker locationPicker;
    private EventsContract events;
    private Clock clock;

    private final Map<PaletteLabel, PaletteInformation> stock = new HashMap<>();

    public interface EventsContract {
        void emit(Registered event);

        void emit(Stored event);

        void emit(Picked event);

        void emit(Locked lock);
    }

    public void registerNew(RegisterNew registerNew) {
        assert refNo.equals(registerNew.getPaletteLabel().getRefNo());
        if (stock.containsKey(registerNew.getPaletteLabel())) {
            return;
        }
        Set<String> violations = new HashSet<>();
        BoxLabel first = registerNew.getScannedBoxes().get(0);

        if (!registerNew.getScannedBoxes().isEmpty()) {
            violations.add("Palette without boxes is cannot be registered");
        }
        if (registerNew.getScannedBoxes().stream().allMatch(box ->
                first.getRefNo().equals(box.getRefNo())
                        && first.getBoxType().equals(box.getBoxType()))) {
            violations.add("Not all boxes have matching product");
        }
        if (registerNew.getPaletteLabel().getRefNo().equals(first.getRefNo())) {
            violations.add("Palette label not match box label");
        }
        ValidationResult validation = new ValidationResult(violations);

        Location suggestedLocation = validation.isValid()
                ? locationPicker.suggestFor(registerNew.getPaletteLabel().getRefNo())
                : Location.quarantine();

        LocalDateTime producedAt = LocalDateTime.now(clock);
        Registered event = new Registered(
                registerNew.getPaletteLabel(),
                registerNew.getScannedBoxes(),
                producedAt,
                suggestedLocation,
                validation
        );
        apply(event);
        events.emit(event);

        if (!validation.isValid()) {
            Locked lock = new Locked(registerNew.getPaletteLabel());
            events.emit(lock);
        }
    }

    public void pick(Pick pick) {
        assert refNo.equals(pick.getPaletteLabel().getRefNo());
        Location fromLocation = stock.get(pick.getPaletteLabel()).currentLocation;
        Picked event = new Picked(pick.getPaletteLabel(), pick.getUser(),
                fromLocation, Location.onTheMove(pick.getUser()));
        apply(event);
        events.emit(event);
    }

    public void store(Store store) {
        assert refNo.equals(store.getPaletteLabel().getRefNo());
        Stored event = new Stored(
                store.getPaletteLabel(),
                store.getLocation());
        apply(event);
        events.emit(event);
    }

    public void delivered(Delivered event) {
        stock.remove(event.getPaletteLabel());
    }

    public void destroyed(Destroyed event) {
        stock.remove(event.getPaletteLabel());
    }

    public Location getLocation(PaletteLabel paletteLabel) {
        if (stock.containsKey(paletteLabel)) {
            return stock.get(paletteLabel).getCurrentLocation();
        } else {
            return Location.unknown();
        }
    }

    protected void apply(Registered event) {
        stock.put(event.getPaletteLabel(), new PaletteInformation(event));
    }

    protected void apply(Picked event) {
        stock.get(event.getPaletteLabel())
                .setCurrentLocation(event.getOnTheMoveLocation());
    }

    protected void apply(Stored event) {
        stock.get(event.getPaletteLabel())
                .setCurrentLocation(event.getLocation());
    }

    protected void apply(Locked event) {
    }

    @Data
    protected class PaletteInformation {
        private final Registered init;
        private Location currentLocation = Location.production();
    }
}

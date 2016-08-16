package warehouse.products;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import warehouse.BoxLabel;
import warehouse.PaletteLabel;
import warehouse.locations.BasicLocationPicker;
import warehouse.locations.Location;
import warehouse.locations.PreferredLocationPicker;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by michal on 15.07.2016.
 */
@Setter
@Accessors(fluent = true)
public class ProductStockBuilder {

    private final String refNo;
    private PaletteValidator validator = new PaletteValidator();
    private PreferredLocationPicker locationsPicker = new BasicLocationPicker(Collections.emptyMap());
    private EventsAssert events = new EventsAssert();
    private Clock clock = Clock.systemDefaultZone();

    public static ProductStockBuilder forRefNo(String refNo) {
        return new ProductStockBuilder(refNo);
    }

    public static RefNoLocation l(String refNo, Location location) {
        return new RefNoLocation(refNo, location);
    }

    public ProductStock build() {
        return new ProductStock(refNo, validator, locationsPicker, events, clock);
    }

    public ProductStockBuilder locationsPicker(RefNoLocation... locations) {
        locationsPicker = new BasicLocationPicker(Stream.of(locations)
                .collect(Collectors.toMap(RefNoLocation::refNo, RefNoLocation::location)));
        return this;
    }

    public History history() {
        return new History(build());
    }

    private ProductStockBuilder(String refNo) {
        this.refNo = refNo;
    }

    public class History {

        private final ProductStock object;

        private History(ProductStock object) {
            this.object = object;
        }

        public ProductStock get() {
            return object;
        }

        public History newPalette(PaletteLabel paletteLabel) {
            return newPalette(paletteLabel, LocalDateTime.now(clock),
                    locationsPicker.suggestFor(paletteLabel.getRefNo()));
        }

        public History newPalette(PaletteLabel paletteLabel, LocalDateTime producedAt) {
            return newPalette(paletteLabel, producedAt, locationsPicker.suggestFor(paletteLabel.getRefNo()));
        }

        public History newPalette(PaletteLabel paletteLabel, LocalDateTime producedAt, Location preferredLocation) {
            ReadyToStore event = new ReadyToStore(
                    paletteLabel, someBoxesFor(paletteLabel.getRefNo()), producedAt, preferredLocation
            );
            object.handle(event);
            return this;
        }

        public History stored(PaletteLabel paletteLabel, Location location) {
            object.handle(new Stored(paletteLabel, location));
            return this;
        }

        public History picked(PaletteLabel paletteLabel, Location location, String user) {
            object.handle(new Picked(paletteLabel, user, location, Location.onTheMove(user)));
            return this;
        }

        private List<BoxLabel> someBoxesFor(String refNo) {
            return Collections.nCopies(10, new BoxLabel(refNo, 25, "A"));
        }
    }

    @Getter
    @AllArgsConstructor
    public static class RefNoLocation {
        private final String refNo;
        private final Location location;
    }
}

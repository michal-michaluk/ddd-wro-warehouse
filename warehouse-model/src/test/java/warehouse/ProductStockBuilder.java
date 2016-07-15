package warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import warehouse.locations.BasicLocationPicker;
import warehouse.locations.Location;

import java.time.Clock;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by michal on 15.07.2016.
 */
@Setter
@Accessors(fluent = true)
public class ProductStockBuilder {

    private final String refNo;
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
        return new ProductStock(refNo, events, locationsPicker, clock);
    }

    public ProductStockBuilder locationsPicker(RefNoLocation... locations) {
        locationsPicker = new BasicLocationPicker(Stream.of(locations)
                .collect(Collectors.toMap(RefNoLocation::refNo, RefNoLocation::location)));
        return this;
    }

    public History history() {
        return History.forInstance(build());
    }

    private ProductStockBuilder(String refNo) {
        this.refNo = refNo;
    }

    public static class History {

        private final ProductStock object;

        public static History forInstance(ProductStock object) {
            return new History(object);
        }

        public ProductStock get() {
            return object;
        }

        private History(ProductStock object) {
            this.object = object;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class RefNoLocation {
        private final String refNo;
        private final Location location;
    }
}

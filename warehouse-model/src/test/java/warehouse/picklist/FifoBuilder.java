package warehouse.picklist;

import lombok.Setter;
import lombok.experimental.Accessors;
import warehouse.PaletteLabel;
import warehouse.locations.Location;
import warehouse.products.Delivered;
import warehouse.products.ReadyToStore;
import warehouse.quality.Destroyed;
import warehouse.quality.Locked;
import warehouse.quality.Unlocked;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by michal on 15.07.2016.
 */
@Setter
@Accessors(fluent = true)
public class FifoBuilder {

    private final Map<PaletteLabel, Location> locations = new HashMap<>();
    private final Map<String, Fifo.PerProduct> products = new HashMap<>();
    private Location unknown = Location.unknown();

    public static FifoBuilder builder() {
        return new FifoBuilder();
    }

    public Fifo build() {
        return new Fifo(p -> locations.getOrDefault(p, unknown), this::product);
    }

    private Fifo.PerProduct product(String refNo) {
        return products.computeIfAbsent(refNo, ref -> new Fifo.PerProduct());
    }

    public History history() {
        return new History(build());
    }

    public class History {

        private final Fifo object;

        private History(Fifo object) {
            this.object = object;
        }

        public Fifo get() {
            return object;
        }

        public History newPalette(PaletteLabel paletteLabel, LocalDateTime producedAt, Location storedAt) {
            Fifo.PerProduct product = product(paletteLabel.getRefNo());
            product.handle(new ReadyToStore(paletteLabel, Collections.emptyList(), producedAt, storedAt));
            locations.put(paletteLabel, storedAt);
            return this;
        }

        public History delivered(PaletteLabel paletteLabel) {
            Fifo.PerProduct product = product(paletteLabel.getRefNo());
            product.handle(new Delivered(paletteLabel));
            return this;
        }

        public History locked(PaletteLabel paletteLabel) {
            Fifo.PerProduct product = product(paletteLabel.getRefNo());
            product.handle(new Locked(paletteLabel));
            return this;
        }

        public History unlocked(PaletteLabel paletteLabel) {
            Fifo.PerProduct product = product(paletteLabel.getRefNo());
            product.handle(new Unlocked(paletteLabel));
            return this;
        }

        public History destroyed(PaletteLabel paletteLabel) {
            Fifo.PerProduct product = product(paletteLabel.getRefNo());
            product.handle(new Destroyed(paletteLabel));
            return this;
        }
    }
}

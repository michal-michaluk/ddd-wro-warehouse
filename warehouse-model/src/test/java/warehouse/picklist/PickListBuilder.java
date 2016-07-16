package warehouse.picklist;

import lombok.Setter;
import lombok.experimental.Accessors;
import warehouse.PaletteLabel;
import warehouse.locations.Location;
import warehouse.products.ReadyToStore;
import warehouse.products.Stored;

import java.time.LocalDateTime;

/**
 * Created by michal on 15.07.2016.
 */
@Setter
@Accessors(fluent = true)
public class PickListBuilder {

    private final Fifo object;

    public static PickListBuilder fifo() {
        return new PickListBuilder(new Fifo());
    }

    public PickListBuilder newPalette(PaletteLabel paletteLabel, LocalDateTime producedAt, Location storedAt) {
        object.handle(new ReadyToStore(paletteLabel, producedAt, storedAt));
        object.handle(new Stored(paletteLabel, storedAt));
        return this;
    }

    public PickListBuilder movedTo(PaletteLabel paletteLabel, Location location) {
        object.handle(new Stored(paletteLabel, location));
        return this;
    }

    public Fifo get() {
        return object;
    }

    private PickListBuilder(Fifo object) {
        this.object = object;
    }
}

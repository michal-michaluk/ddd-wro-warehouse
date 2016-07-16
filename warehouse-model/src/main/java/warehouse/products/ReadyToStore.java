package warehouse.products;

import lombok.Value;
import warehouse.PaletteLabel;
import warehouse.locations.Location;

import java.time.LocalDateTime;

/**
 * Created by michal on 08.06.2016.
 */
@Value
public class ReadyToStore {
    private final PaletteLabel paletteLabel;
    private final LocalDateTime readyAt;
    private final Location preferredLocation;
}

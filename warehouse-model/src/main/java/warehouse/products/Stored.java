package warehouse.products;

import lombok.Value;
import warehouse.PaletteLabel;
import warehouse.locations.Location;

/**
 * Created by michal on 13.07.2016.
 */
@Value
public class Stored {
    private final PaletteLabel paletteLabel;
    private final Location location;
}

package warehouse;

import lombok.Value;
import warehouse.locations.Location;

/**
 * Created by michal on 13.07.2016.
 */
@Value
public class Picked {
    private final PaletteLabel paletteLabel;
    private final String user;
    private final Location lastKnownLocation;
}

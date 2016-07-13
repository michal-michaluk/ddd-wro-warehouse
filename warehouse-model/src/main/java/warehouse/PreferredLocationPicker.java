package warehouse;

import warehouse.locations.Location;

/**
 * Created by michal on 08.06.2016.
 */
public interface PreferredLocationPicker {
    Location suggestLocationFor(PaletteLabel paletteLabel);
}

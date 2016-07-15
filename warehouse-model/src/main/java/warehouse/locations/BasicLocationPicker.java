package warehouse.locations;

import lombok.AllArgsConstructor;
import warehouse.PaletteLabel;
import warehouse.PreferredLocationPicker;

import java.util.Map;

/**
 * Created by michal on 15.07.2016.
 */
@AllArgsConstructor
public class BasicLocationPicker implements PreferredLocationPicker {
    private final Map<String, Location> perRefNo;
    private final Location def = new Location("<no suggestion>");

    @Override
    public Location suggestLocationFor(PaletteLabel paletteLabel) {
        return perRefNo.getOrDefault(paletteLabel.getRefNo(), def);
    }
}

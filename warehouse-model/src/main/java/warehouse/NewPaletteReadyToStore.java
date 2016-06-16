package warehouse;

import lombok.Value;

/**
 * Created by michal on 08.06.2016.
 */
@Value
public class NewPaletteReadyToStore {
    private final PaletteLabel label;
    private final String preferredLocation;
}

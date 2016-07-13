package warehouse;

import lombok.Value;

/**
 * Created by michal on 13.07.2016.
 */
@Value
public class Pick {
    private final PaletteLabel paletteLabel;
    private final String user;
}

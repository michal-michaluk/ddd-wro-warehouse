package warehouse.products;

import lombok.Value;
import warehouse.PaletteLabel;

/**
 * Created by michal on 13.07.2016.
 */
@Value
public class Pick {
    private final PaletteLabel paletteLabel;
    private final String user;
}

package warehouse.products;

import lombok.Value;
import warehouse.PaletteLabel;

/**
 * Created by michal on 10.08.2016.
 */
@Value
public class Delivered {
    private final PaletteLabel paletteLabel;
}

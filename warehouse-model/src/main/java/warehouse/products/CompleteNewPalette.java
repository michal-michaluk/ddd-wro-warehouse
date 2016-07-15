package warehouse.products;

import lombok.Value;
import warehouse.BoxLabel;
import warehouse.PaletteLabel;

import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 08.06.2016.
 */
@Value
public class CompleteNewPalette {
    private final PaletteLabel paletteLabel;
    private final List<BoxLabel> scannedBoxes;

    public CompleteNewPalette(PaletteLabel paletteLabel, List<BoxLabel> scannedBoxes) {
        this.paletteLabel = paletteLabel;
        this.scannedBoxes = Collections.unmodifiableList(scannedBoxes);
    }
}

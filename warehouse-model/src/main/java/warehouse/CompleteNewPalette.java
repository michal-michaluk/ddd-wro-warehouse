package warehouse;

import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 08.06.2016.
 */
@Value
public class CompleteNewPalette {
    private final PaletteLabel label;
    private final List<BoxScan> scannedBoxes;

    public CompleteNewPalette(PaletteLabel label, List<BoxScan> scannedBoxes) {
        this.label = label;
        this.scannedBoxes = Collections.unmodifiableList(scannedBoxes);
    }
}

package warehouse.products;

import lombok.AllArgsConstructor;
import lombok.Value;
import warehouse.BoxLabel;
import warehouse.PaletteLabel;
import warehouse.locations.Location;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static warehouse.products.ValidationResult.valid;

/**
 * Created by michal on 08.06.2016.
 */
@Value
@AllArgsConstructor
public class Registered {
    private final PaletteLabel paletteLabel;
    private final List<BoxLabel> scannedBoxes;
    private final LocalDateTime readyAt;
    private final Location preferredLocation;
    private final ValidationResult validationResult;

    public Registered(PaletteLabel paletteLabel, List<BoxLabel> scannedBoxes,
                      LocalDateTime readyAt, Location preferredLocation) {
        this.paletteLabel = paletteLabel;
        this.scannedBoxes = Collections.unmodifiableList(scannedBoxes);
        this.readyAt = readyAt;
        this.preferredLocation = preferredLocation;
        validationResult = valid();
    }
}

package warehouse.products;

import lombok.AllArgsConstructor;
import lombok.Value;
import warehouse.BoxLabel;
import warehouse.PaletteLabel;
import warehouse.locations.Location;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static warehouse.products.PaletteValidator.ValidationResult.valid;

/**
 * Created by michal on 08.06.2016.
 */
@Value
@AllArgsConstructor
public class ReadyToStore {
    private final PaletteLabel paletteLabel;
    private final List<BoxLabel> scannedBoxes;
    private final LocalDateTime readyAt;
    private final Location preferredLocation;
    private final PaletteValidator.ValidationResult validationResult;

    public ReadyToStore(PaletteLabel paletteLabel, List<BoxLabel> scannedBoxes,
                        LocalDateTime readyAt, Location preferredLocation) {
        this.paletteLabel = paletteLabel;
        this.scannedBoxes = Collections.unmodifiableList(scannedBoxes);
        this.readyAt = readyAt;
        this.preferredLocation = preferredLocation;
        validationResult = valid();
    }
}

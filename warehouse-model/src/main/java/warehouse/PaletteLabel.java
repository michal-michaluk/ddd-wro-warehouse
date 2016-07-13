package warehouse;

import lombok.Value;

/**
 * Created by michal on 08.06.2016.
 */
@Value
public class PaletteLabel {
    private final String id; // P-300900-123
    private final String refNo;

    public static PaletteLabel scan(String label) {
        return new PaletteLabel(label, label); // TODO MM
    }
}

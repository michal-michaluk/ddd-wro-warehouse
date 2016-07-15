package warehouse;

import lombok.Value;

/**
 * Created by michal on 08.06.2016.
 */
@Value
public class PaletteLabel {
    private final String id;
    private final String refNo;

    public static PaletteLabel scan(String label) {
        // TODO parse: P-<refNo>-<unique-suffix>
        return new PaletteLabel(label, label);
    }
}

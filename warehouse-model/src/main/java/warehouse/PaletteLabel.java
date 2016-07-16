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
        if (!label.startsWith("P-")) {
            throw new IllegalArgumentException("Label need to start with 'P-' characters, ex: P-900300-8A3");
        }
        if (label.indexOf('-', 2) == -1) {
            throw new IllegalArgumentException("Label need to have two '-' characters, ex: P-900300-8A3");
        }
        String refNo = label.substring(2, label.indexOf('-', 2));
        if (refNo.isEmpty()) {
            throw new IllegalArgumentException("Label has empty refNo, format: P-<refNo>-<index>");
        }
        if (label.indexOf('-', 2) == label.length() - 1) {
            throw new IllegalArgumentException("Label has empty index, format: P-<refNo>-<index>");
        }
        return new PaletteLabel(label, refNo);
    }
}

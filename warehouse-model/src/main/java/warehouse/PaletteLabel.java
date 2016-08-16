package warehouse;

import lombok.Value;

/**
 * Created by michal on 08.06.2016.
 */
@Value
public class PaletteLabel {
    private final String id;
    private final String refNo;

    @Override
    public String toString() {
        return id;
    }
}

package quality;

import lombok.Value;
import warehouse.PaletteLabel;

import java.util.List;

/**
 * Created by michal on 18.08.2016.
 */
@Value
public class QualityReport {

    private final List<PaletteLabel> locked;
    private final List<Recovered> recovered;
    private final List<PaletteLabel> destroyed;

    @Value
    public static class Recovered {
        private final PaletteLabel label;
        private final int recovered;
        private final int scraped;
    }
}

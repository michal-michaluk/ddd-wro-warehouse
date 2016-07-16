package warehouse;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by michal on 15.07.2016.
 */
public class LabelGenerator {

    private AtomicInteger index = new AtomicInteger();

    public PaletteLabel palette(String refNo) {
        String suffix = String.format("%04X", index.getAndIncrement());
        return new PaletteLabel("P-" + refNo + "-" + suffix, refNo);
    }
}

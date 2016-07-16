package warehouse;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by michal on 15.07.2016.
 */
public class TLabelsFormats implements Labels {

    private AtomicInteger index = new AtomicInteger();

    @Override
    public PaletteLabel newPalette(String refNo) {
        String suffix = String.format("%04X", index.getAndIncrement());
        return new PaletteLabel("P-" + refNo + "-" + suffix, refNo);
    }

    @Override
    public PaletteLabel scanPalette(String label) {
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

    @Override
    public BoxLabel scanBox(String label) {
        // TODO parse: B-<refNo>-<quantity>-<boxType>
        return new BoxLabel("", 0, "");
    }
}

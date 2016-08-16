package warehouse;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by michal on 15.07.2016.
 */
public class TLabelsFormats implements Labels {

    private final AtomicInteger index;

    public TLabelsFormats(int lastIndex) {
        this.index = new AtomicInteger(lastIndex);
    }

    @Override
    public PaletteLabel newPalette(String refNo) {
        String suffix = String.format("%04X", index.getAndIncrement());
        return new PaletteLabel("P-" + refNo + "-" + suffix, refNo);
    }

    @Override
    public PaletteLabel scanPalette(String label) {
        String[] pieces = label.split("-");
        if (!label.startsWith("P-")) {
            throw new IllegalArgumentException("Label need to start with 'P-' characters, format: P-<refNo>-<index>, ex: P-900300-8A3");
        }
        if (pieces.length != 3) {
            throw new IllegalArgumentException("Label need to have two '-' characters, format: P-<refNo>-<index>, ex: P-900300-8A3");
        }
        if (pieces[1].isEmpty()) {
            throw new IllegalArgumentException("Label has empty refNo, format: P-<refNo>-<index>, ex: P-900300-8A3");
        }
        if (pieces[2].isEmpty()) {
            throw new IllegalArgumentException("Label has empty index, format: P-<refNo>-<index>, ex: P-900300-8A3");
        }
        return new PaletteLabel(label, pieces[1]);
    }

    @Override
    public BoxLabel scanBox(String label) {
        String[] pieces = label.split("-");
        if (!label.startsWith("B-")) {
            throw new IllegalArgumentException("Label need to start with 'B-' characters, format: B-<refNo>-<quantity>-<boxType>, ex: B-900300-24-B");
        }
        if (pieces.length != 4) {
            throw new IllegalArgumentException("Label need to have three '-' characters, format: B-<refNo>-<quantity>-<boxType>, ex: B-900300-24-B");
        }
        return new BoxLabel(pieces[1], Integer.valueOf(pieces[2]), pieces[3]);
    }
}

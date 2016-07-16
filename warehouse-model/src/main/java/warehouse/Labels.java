package warehouse;

/**
 * Created by michal on 16.07.2016.
 */
public interface Labels {
    PaletteLabel newPalette(String refNo);

    PaletteLabel scanPalette(String label);

    BoxLabel scanBox(String label);
}

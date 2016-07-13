package warehouse;

/**
 * Created by michal on 08.06.2016.
 */
public interface PreferredLocationPicker {
    String suggestLocationFor(PaletteLabel paletteLabel);
}

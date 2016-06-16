package warehouse;

/**
 * Created by michal on 08.06.2016.
 */
public class BlaBlaBla {

    private Events events;
    private PreferredLocationPicker locationsPicker;

    public BlaBlaBla(Events events, PreferredLocationPicker locationsPicker) {
        this.events = events;
        this.locationsPicker = locationsPicker;
    }

    public void completeNewPalette(CompleteNewPalette completeNewPalette) {
        // sprawdzamy czy zeskanowane boxy sa z zgodne z produktem na etykiecie palety
        // sprawdzamy czy wszystkie zeskanowane boxy sa tego samego typu
        // wyliczamy preferowan lokacje
        // strzel eventem NewPaletteReadyToStore

        NewPaletteReadyToStore event = new NewPaletteReadyToStore(
                completeNewPalette.getLabel(),
                locationsPicker.sagestLocationFor(completeNewPalette.getLabel())
        );
        events.fire(event);
    }

    public interface Events {
        void fire(NewPaletteReadyToStore newPaletteReadyToStore);
    }
}

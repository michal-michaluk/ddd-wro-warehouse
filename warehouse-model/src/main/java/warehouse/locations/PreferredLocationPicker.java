package warehouse.locations;

/**
 * Created by michal on 08.06.2016.
 */
public interface PreferredLocationPicker {
    Location suggestFor(String refNo);
}

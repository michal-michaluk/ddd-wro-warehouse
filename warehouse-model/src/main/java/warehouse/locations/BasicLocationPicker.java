package warehouse.locations;

import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Created by michal on 15.07.2016.
 */
@AllArgsConstructor
public class BasicLocationPicker implements PreferredLocationPicker {
    private final Map<String, Location> perRefNo;
    private final Location def = new Location("<no suggestion>");

    @Override
    public Location suggestFor(String refNo) {
        return perRefNo.getOrDefault(refNo, def);
    }
}

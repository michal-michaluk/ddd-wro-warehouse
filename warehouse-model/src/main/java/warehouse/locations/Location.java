package warehouse.locations;

import lombok.Value;

/**
 * Created by michal on 13.07.2016.
 */
@Value
public class Location {
    private final String location;

    public static Location production() {
        return new Location("production");
    }

    public static Location onTheMove() {
        return new Location("");
    }
}

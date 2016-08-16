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

    public static Location quarantine() {
        return new Location("quarantine");
    }

    public static Location onTheMove(String user) {
        return new Location("picked by " + user);
    }

    public static Location unknown() {
        return new Location("<unknown>");
    }
}

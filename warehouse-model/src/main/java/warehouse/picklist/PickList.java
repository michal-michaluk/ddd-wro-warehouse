package warehouse.picklist;

import lombok.Builder;
import lombok.Value;
import warehouse.PaletteLabel;
import warehouse.locations.Location;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by michal on 15.07.2016.
 */
@Value
@Builder
public class PickList {

    private final List<Pick> picks;

    private PickList(List<Pick> picks) {
        this.picks = Collections.unmodifiableList(picks);
    }

    @Value
    public static class Pick {
        private final PaletteLabel paletteLabel;
        private final Location location;
    }

    public static class PickListBuilder {
        public PickListBuilder add(PaletteLabel paletteLabel, Location location) {
            picks.add(new Pick(paletteLabel, location));
            return this;
        }

        private PickListBuilder() {
            picks = new LinkedList<>();
        }
    }
}

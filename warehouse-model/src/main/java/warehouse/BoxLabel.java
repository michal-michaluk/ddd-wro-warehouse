package warehouse;

import lombok.Value;

/**
 * Created by michal on 08.06.2016.
 */
@Value
public class BoxLabel {
    private final String refNo;
    private final int quantity;
    private final String boxType;
}

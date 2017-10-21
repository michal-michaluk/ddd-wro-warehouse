package warehouse.products;

import tools.AgentQueue;
import warehouse.PaletteLabel;
import warehouse.locations.Location;

import java.util.concurrent.CompletionStage;

/**
 * Created by michal on 10.10.2017.
 */
public class ProductStockAgent {

    private final String refNo;
    private final AgentQueue queue;
    private final ProductStock object;

    public ProductStockAgent(String refNo, ProductStock object, AgentQueue queue) {
        this.refNo = refNo;
        this.object = object;
        this.queue = queue;
    }

    public String getRefNo() {
        return refNo;
    }

    public CompletionStage<?> registerNew(RegisterNew registerNew) {
        return queue.command(() -> object.registerNew(registerNew));
    }

    public CompletionStage<?> pick(Pick pick) {
        return queue.command(() -> object.pick(pick));
    }

    public CompletionStage<?> store(Store store) {
        return queue.command(() -> object.store(store));
    }

    public CompletionStage<?> delivered(Delivered event) {
        return queue.command(() -> object.delivered(event));
    }

    public CompletionStage<Location> getLocation(PaletteLabel paletteLabel) {
        return queue.query(() -> object.getLocation(paletteLabel));
    }

    public Location getLocationSync(PaletteLabel paletteLabel) {
        return queue.querySync(() -> object.getLocation(paletteLabel));
    }
}

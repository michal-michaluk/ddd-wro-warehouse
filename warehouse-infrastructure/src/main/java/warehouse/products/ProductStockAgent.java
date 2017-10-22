package warehouse.products;

import lombok.AllArgsConstructor;
import tools.AgentQueue;
import warehouse.PaletteLabel;
import warehouse.locations.Location;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Created by michal on 10.10.2017.
 */
@AllArgsConstructor
public class ProductStockAgent {

    private final String refNo;
    private final ProductStock object;
    private final ProductStockEventSourcingRepository.ProductStockEventsHandler events;
    private final AgentQueue queue;

    public String getRefNo() {
        return refNo;
    }

    public CompletionStage<List<Object>> registerNew(RegisterNew registerNew) {
        return queue.command(() -> object.registerNew(registerNew))
                .thenApply(o -> events.getLast());
    }

    public CompletionStage<List<Object>> pick(Pick pick) {
        return queue.command(() -> object.pick(pick))
                .thenApply(o -> events.getLast());
    }

    public CompletionStage<List<Object>> store(Store store) {
        return queue.command(() -> object.store(store))
                .thenApply(o -> events.getLast());
    }

    public CompletionStage<List<Object>> delivered(Delivered event) {
        return queue.command(() -> object.delivered(event))
                .thenApply(o -> events.getLast());
    }

    public CompletionStage<Location> getLocation(PaletteLabel paletteLabel) {
        return queue.query(() -> object.getLocation(paletteLabel));
    }

    public Location getLocationSync(PaletteLabel paletteLabel) {
        return queue.querySync(() -> object.getLocation(paletteLabel));
    }
}

package warehouse.picklist;

import lombok.Data;
import warehouse.PaletteLabel;
import warehouse.locations.Location;
import warehouse.products.ReadyToStore;
import warehouse.products.Stored;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by michal on 15.07.2016.
 */
public class Fifo {

    private final Map<String, PerProduct> products = new ConcurrentHashMap<>();

    public PickList pickList(Order order) {
        PickList.PickListBuilder pickList = PickList.builder();
        for (Order.Item item : order.getItems()) {
            product(item.getRefNo())
                    .first(item.getAmount()).forEach(paletteInfo ->
                    pickList.add(paletteInfo.paletteLabel, paletteInfo.location));
        }
        return pickList.build();
    }

    public void handle(ReadyToStore event) {
        product(event.getPaletteLabel().getRefNo())
                .add(event);
    }

    public void handle(Stored event) {
        product(event.getPaletteLabel().getRefNo())
                .move(event);
    }

    private PerProduct product(String refNo) {
        return products.computeIfAbsent(refNo, ref -> new PerProduct());
    }

    private static class PerProduct {
        private final PriorityQueue<PaletteInfo> queue = new PriorityQueue<>(
                Comparator.comparing(PaletteInfo::getReadyAt));

        private final Map<PaletteLabel, PaletteInfo> index = new HashMap<>();

        private synchronized List<PaletteInfo> first(int amount) {
            return queue.stream().limit(amount).collect(Collectors.toList());
        }

        private synchronized void add(ReadyToStore event) {
            PaletteInfo entry = new PaletteInfo(event.getPaletteLabel(), event.getReadyAt());
            queue.add(entry);
            index.put(event.getPaletteLabel(), entry);
        }

        private synchronized void move(Stored stored) {
            PaletteInfo moved = index.get(stored.getPaletteLabel());
            moved.setLocation(stored.getLocation());
        }

        private synchronized void remove(PaletteLabel paletteLabel) {
            PaletteInfo removed = index.remove(paletteLabel);
            queue.remove(removed);
        }
    }

    @Data
    private static class PaletteInfo {
        private final PaletteLabel paletteLabel;
        private final LocalDateTime readyAt;
        private Location location;
    }

}

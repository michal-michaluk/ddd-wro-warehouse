package quality;

import warehouse.EventMappings;
import warehouse.PaletteLabel;
import warehouse.products.ProductStockExtendedRepository;
import warehouse.quality.Destroyed;
import warehouse.quality.Locked;
import warehouse.quality.Unlocked;

/**
 * Created by michal on 20.08.2016.
 */
public class QualityReportService {

    private final ProductStockExtendedRepository stocks;
    private final EventMappings.ExternalEvents events;

    public QualityReportService(ProductStockExtendedRepository stocks, EventMappings mappings) {
        this.stocks = stocks;
        this.events = mappings.externalEvents();
    }

    public void process(QualityReport report) {
        for (PaletteLabel label : report.getLocked()) {
            Locked event = new Locked(label);
            stocks.persist(label, event);
            events.emit(event);
        }

        for (QualityReport.Recovered status : report.getRecovered()) {
            Unlocked event = new Unlocked(status.getLabel(), status.getRecovered(), status.getScraped());
            stocks.persist(status.getLabel(), event);
            events.emit(event);
        }

        for (PaletteLabel label : report.getDestroyed()) {
            Destroyed event = new Destroyed(label);
            stocks.persist(label, event);
            events.emit(event);
        }
    }
}

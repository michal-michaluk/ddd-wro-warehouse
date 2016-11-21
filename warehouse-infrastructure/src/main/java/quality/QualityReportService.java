package quality;

import warehouse.EventMappings;
import warehouse.PaletteLabel;
import warehouse.products.ProductStockFileRepository;
import warehouse.quality.Destroyed;
import warehouse.quality.Locked;
import warehouse.quality.Unlocked;

/**
 * Created by michal on 20.08.2016.
 */
public class QualityReportService {

    private final ProductStockFileRepository stocks;
    private final EventMappings.ExternalEvents events;

    public QualityReportService(ProductStockFileRepository stocks, EventMappings mappings) {
        this.stocks = stocks;
        this.events = mappings.new ExternalEvents();
    }

    public void process(QualityReport report) {
        for (PaletteLabel label : report.getLocked()) {
            Locked event = new Locked(label);
            stocks.persist(label.getRefNo(), event);
            events.emit(event);
        }

        for (QualityReport.Recovered status : report.getRecovered()) {
            Unlocked event = new Unlocked(status.getLabel(), status.getRecovered(), status.getScraped());
            stocks.persist(status.getLabel().getRefNo(), event);
            events.emit(event);
        }

        for (PaletteLabel label : report.getDestroyed()) {
            Destroyed event = new Destroyed(label);
            stocks.persist(label.getRefNo(), event);
            events.emit(event);
        }
    }
}

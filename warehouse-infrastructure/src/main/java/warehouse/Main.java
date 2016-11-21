package warehouse;

import quality.QualityReportService;
import rest.api.v1.Inbox;
import rest.api.v1.ProductStocks;
import warehouse.picklist.FifoViewRepository;
import warehouse.products.ProductStockFileRepository;

/**
 * Created by michal on 13.07.2016.
 */
public class Main {

    public static void main(String[] args) {
        EventMappings mappings = new EventMappings();
        TLabelsFormats labels = new TLabelsFormats(0);
        ProductStockFileRepository stocks = new ProductStockFileRepository(mappings);
        FifoViewRepository fifo = new FifoViewRepository(stocks);
        QualityReportService quality = new QualityReportService(stocks, mappings);
        mappings.dependencies(stocks, fifo);

        new ProductStocks(labels, stocks, fifo).exposeApi();
        new Inbox(labels, quality).exposeApi();
    }
}

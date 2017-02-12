package warehouse;

import org.sql2o.Sql2o;
import quality.QualityReportService;
import rest.api.v1.Inbox;
import rest.api.v1.ProductStocks;
import warehouse.picklist.FifoViewProjection;
import warehouse.products.ProductStockDatabaseRepository;

/**
 * Created by michal on 13.07.2016.
 */
public class Main {

    public static void main(String[] args) {
        Sql2o database = new Sql2o(args[0], args[1], args[2]);

        EventMappings mappings = new EventMappings();
        TLabelsFormats labels = new TLabelsFormats(0);
        ProductStockDatabaseRepository stocks = new ProductStockDatabaseRepository(mappings, database);
        FifoViewProjection fifo = new FifoViewProjection(stocks);
        QualityReportService quality = new QualityReportService(stocks, mappings);
        mappings.dependencies(stocks, fifo);

        new ProductStocks(labels, stocks, fifo).exposeApi();
        new Inbox(labels, quality).exposeApi();
    }
}

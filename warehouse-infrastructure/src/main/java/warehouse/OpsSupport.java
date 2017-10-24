package warehouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import warehouse.locations.PreferredLocationPicker;
import warehouse.products.ProductStockAgent;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by michal on 20.10.2017.
 */
public class OpsSupport {

    private static final Logger PRODUCT_STOCK = LoggerFactory.getLogger("warehouse.OpsSupport.PRODUCT_STOCK");
    private static final Logger FIFO_VIEW = LoggerFactory.getLogger("warehouse.OpsSupport.FIFO_VIEW");
    private static final Logger QUALITY_REPORT = LoggerFactory.getLogger("warehouse.OpsSupport.QUALITY_REPORT");
    private static final Logger MASTER_DATA = LoggerFactory.getLogger("warehouse.OpsSupport.MASTER_DATA");

    public void initialisingStockForNewProduct(String refNo, PreferredLocationPicker locationPicker) {
        PRODUCT_STOCK.info("{}: initialising stock for new refNo: {}, default setting: {}, {}",
                refNo, refNo, toSimpleName(locationPicker));
    }

    public <T> T executedCommandOnProductStock(Request request, ProductStockAgent stock, Object command, Throwable throwable) {
        if (throwable == null) {
            executedCommandOnProductStock(stock.getRefNo(), request, command);
        } else {
            failedToExecuteCommandOnProductStock(stock.getRefNo(), request, command, throwable);
        }
        return null;
    }

    private void executedCommandOnProductStock(String refNo, Request request, Object command) {
        String commandType = toSimpleName(command);
        if (!PRODUCT_STOCK.isDebugEnabled()) {
            PRODUCT_STOCK.info("{}: executed command: {}, content: {}",
                    refNo, commandType, command);
        } else {
            PRODUCT_STOCK.debug("{}: executed command: {}, content: {} parsed from REST body: {}",
                    refNo, commandType, command, toString(request));
        }
    }

    private void failedToExecuteCommandOnProductStock(String refNo, Request request, Object command, Throwable throwable) {
        PRODUCT_STOCK.error("{}: cannot execute command: {}, content: {}, parsed from REST body: {}",
                refNo, toSimpleName(command), command, toString(request), throwable);
    }

    public <T> T appliedExternalEventOnProductStock(ProductStockAgent stock, Object event, Throwable throwable) {
        if (throwable == null) {
            appliedExternalEventOnProductStock(stock.getRefNo(), event);
        } else {
            failedToApplyExternalEventOnProductStock(stock.getRefNo(), event, throwable);
        }
        return null;
    }

    private void appliedExternalEventOnProductStock(String refNo, Object event) {
        PRODUCT_STOCK.info("{}: applied event: {}, content: {}",
                refNo, toSimpleName(event), event);
    }

    private void failedToApplyExternalEventOnProductStock(String refNo, Object event, Throwable t) {
        PRODUCT_STOCK.error("{}: cannot apply event {}, content: {}",
                refNo, toSimpleName(event), event, t);
    }

    public void failedToApplyEventOnFifo(String refNo, Object event, Throwable t) {
        FIFO_VIEW.error("{}: cannot apply event {}, content: {}",
                refNo, toSimpleName(event), event, t);
    }

    public void failedToReplayEventOnFifo(String refNo, Object event, Throwable t) {
        FIFO_VIEW.error("{}: cannot replay event {}, content: {}",
                refNo, toSimpleName(event), event, t);
    }

    public void executedCommandOnQualityReportService(Request request, Object command) {
        String commandType = toSimpleName(command);
        if (!QUALITY_REPORT.isDebugEnabled()) {
            QUALITY_REPORT.info("executed command: {}, content: {}",
                    commandType, command);
        } else {
            QUALITY_REPORT.debug("executed command: {}, content: {} parsed from REST body: {}",
                    commandType, command, toString(request));
        }
    }

    public void failedToExecuteCommandOnQualityReportService(Request request, Object command, Throwable throwable) {
        QUALITY_REPORT.error("cannot execute command: {}, content: {}, parsed from REST body: {}",
                toSimpleName(command), command, toString(request), throwable);
    }

    public void failedToDeserializeCartDefinition(String refNo, String json) {
        MASTER_DATA.error("{}: failed To deserialize CartDefinition from json: '{}'", refNo, json);
    }

    private String toSimpleName(Object object) {
        return Optional.ofNullable(object)
                .map(e -> e.getClass().getSimpleName())
                .orElse(null);
    }

    private String toString(Request request) {
        return "Request(headers: " + request.headers().stream()
                .collect(Collectors.toMap(Function.identity(), request::headers))
                + ", params: " + request.params()
                + ", body: '" + request.body() + "')";
    }
}

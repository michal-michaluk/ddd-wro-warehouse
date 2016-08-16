package rest.api.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.AllArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Spark;
import tools.RequestStream;
import warehouse.Labels;
import warehouse.locations.Location;
import warehouse.picklist.FifoRepository;
import warehouse.picklist.Order;
import warehouse.products.Pick;
import warehouse.products.ProductStockRepository;
import warehouse.products.RegisterNew;
import warehouse.products.Store;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by michal on 13.07.2016.
 */
@AllArgsConstructor
public class ProductStocks {

    public static final ObjectMapper mapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
            .enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule())
            .registerModule(new JavaTimeModule());

    private Labels labels;
    private ProductStockRepository stocks;
    private FifoRepository fifo;

    public void exposeApi() {
        Spark.get("/api/v1/products", RequestStream.query(RequestStream
                .map((request, response) -> request.queryMap().toMap().entrySet().stream()
                        .reduce(
                                Order.builder(),
                                (order1, entry) -> order1.add(entry.getKey(), Integer.valueOf(entry.getValue()[0])),
                                (o1, o2) -> o2
                        ).build()
                )
                .map((request, response, order) -> fifo.get().pickList(order))
        ), mapper::writeValueAsString);

        Spark.get("/api/v1/products/:palette/location", RequestStream.query(RequestStream
                .map((request, response) -> labels.scanPalette(request.params("palette")))
                .lookup((request, response, label) -> stocks.get(label.getRefNo()))
                .query((request, response, aggregate, label) -> aggregate.getLocation(label))
                .map((request, response, location) -> location.getLocation())
        ));

        Spark.post("/api/v1/products", "application/json", RequestStream.command(RequestStream
                .map((request, response) -> mapper.readTree(request.body()))
                .switchOn((request, response, json) -> json.path("command").asText())
                .when("register", RequestStream
                        .map((Request request, Response response, JsonNode json) -> new RegisterNew(
                                labels.scanPalette(json.path("label").asText()),
                                StreamSupport.stream(json.path("boxes").spliterator(), false)
                                        .map(box -> labels.scanBox(box.asText())).collect(Collectors.toList()))
                        )
                        .lookup((request, response, register) -> stocks.get(register.getPaletteLabel().getRefNo()))
                        .command((request, response, aggregate, register) -> aggregate.registerNew(register))
                )
                .when("pick", RequestStream
                        .map((Request request, Response response, JsonNode json) -> new Pick(
                                labels.scanPalette(json.path("label").asText()),
                                json.path("user").asText())
                        )
                        .lookup((request, response, pick) ->
                                stocks.get(pick.getPaletteLabel().getRefNo())
                        )
                        .command((request, response, aggregate, pick) -> aggregate.pick(pick))
                )
                .when("store", RequestStream
                        .map((Request request, Response response, JsonNode json) -> new Store(
                                labels.scanPalette(json.path("label").asText()),
                                new Location(json.path("location").asText()))
                        )
                        .lookup((request, response, store) -> stocks.get(store.getPaletteLabel().getRefNo()))
                        .command((request, response, aggregate, store) -> aggregate.store(store))
                )
        ));
    }
}

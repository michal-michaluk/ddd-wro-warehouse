package rest.api.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.sun.xml.internal.ws.util.StreamUtils;
import lombok.AllArgsConstructor;
import spark.ResponseTransformer;
import warehouse.Labels;
import warehouse.Repository;
import warehouse.locations.Location;
import warehouse.picklist.Order;
import warehouse.picklist.PickList;
import warehouse.products.Pick;
import warehouse.products.RegisterNew;
import warehouse.products.Store;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.net.HttpURLConnection.HTTP_ACCEPTED;
import static java.net.HttpURLConnection.HTTP_OK;
import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Created by michal on 13.07.2016.
 */
@AllArgsConstructor
public class ProductStocks {

    public static final String APPLICATION_JSON = "application/json";

    static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule())
            .registerModule(new JavaTimeModule());

    private Repository repository;
    private Labels labels;

    public void exposeApi() {
        get("/api/v1/products", (request, response) -> {
            Order.OrderBuilder order = Order.builder();
            request.queryMap().toMap().entrySet().forEach(entry ->
                    order.add(entry.getKey(), Integer.valueOf(entry.getValue()[0])));
            PickList pickList = repository.getFifo().pickList(order.build());

            response.status(HTTP_OK);
            response.type(APPLICATION_JSON);
            return pickList;
        }, mapper::writeValueAsString);

        post("/api/v1/products", "application/json", (request, response) -> {
            String body = request.body();
            JsonNode command = mapper.readTree(body);
            switch (command.get("command").asText()) {
                case "register":{
                    RegisterNew register = new RegisterNew(
                            labels.scanPalette(command.get("label").asText()),
                            StreamSupport.stream(command.get("boxes").spliterator(), false)
                            .map(box -> labels.scanBox(box.asText())).collect(Collectors.toList())
                    );
                    repository.get(register.getPaletteLabel().getRefNo())
                            .ifPresent(o -> o.registerNew(register));
                    break;
                }
                case "pick": {
                    Pick pick = new Pick(
                            labels.scanPalette(command.get("label").asText()),
                            command.get("user").asText()
                    );
                    repository.get(pick.getPaletteLabel().getRefNo())
                            .ifPresent(o -> o.pick(pick));
                    break;
                }
                case "store": {
                    Store store = new Store(
                            labels.scanPalette(command.get("label").asText()),
                            Location.of(command.get("location").asText())
                    );
                    repository.get(store.getPaletteLabel().getRefNo())
                            .ifPresent(o -> o.store(store));
                    break;
                }
            }

            response.status(HTTP_ACCEPTED);
            return null;
        });
    }
}

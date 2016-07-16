package rest.api.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import warehouse.PaletteLabel;
import warehouse.Repository;
import warehouse.products.Pick;

import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_ACCEPTED;
import static java.net.HttpURLConnection.HTTP_OK;
import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Created by michal on 13.07.2016.
 */
public class ProductStocks {
    static ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule())
            .registerModule(new JavaTimeModule());

    private Repository repository;

    public ProductStocks(Repository repository) {
        this.repository = repository;
    }

    public void exposeApi() {
        get("/api/v1/productstocks", (request, response) -> {
            String query = request.queryMap().toMap().entrySet().stream()
                    .map(entry -> entry.getValue()[0] + " palettes of " + entry.getKey())
                    .collect(Collectors.joining("<br/>"));

            response.status(HTTP_OK);
            return "TODO: fifo pick list for:<br/>" + query;
        });

        post("/api/v1/productstocks", "application/json", (request, response) -> {
            String body = request.body();
            JsonNode command = mapper.readTree(body);
            switch (command.get("command").asText()) {
                case "new":
                    break;
                case "pick":
                    Pick pick = new Pick(
                            PaletteLabel.scan(command.get("label").asText()),
                            command.get("user").asText()
                    );
                    repository.get(pick.getPaletteLabel().getRefNo())
                            .ifPresent(o -> o.pick(pick));
                    break;
                case "store":
                    break;
            }

            response.status(HTTP_ACCEPTED);
            return null;
        });
    }
}

package rest.api.v1;

import lombok.AllArgsConstructor;
import spark.Spark;
import warehouse.OpsSupport;
import warehouse.carts.CartDefinitionRepository;

import java.net.HttpURLConnection;
import java.util.Optional;

/**
 * Created by michal on 24.10.2017.
 */
@AllArgsConstructor
public class MasterData {

    private final CartDefinitionRepository cartDefinitions;
    private final OpsSupport support;

    public void exposeApi() {
        exposeCartDefinitions();
    }

    private void exposeCartDefinitions() {
        Spark.path("/api/v1/masterdata/cartDefinitions", () -> {
            Spark.get("/", (request, response) ->
                    cartDefinitions.getDefinedRefNos()
            );
            Spark.get("/:refNo", (request, response) -> {
                Optional<String> definition = cartDefinitions.getJson(request.params("refNo"));
                if (definition.isPresent()) {
                    return definition.get();
                } else {
                    Spark.halt(HttpURLConnection.HTTP_NOT_FOUND);
                    return "";
                }
            });
            Spark.put("/:refNo", "application/json", (request, response) -> {
                        if (cartDefinitions.save(request.params("refNo"), request.body())) {
                            response.status(HttpURLConnection.HTTP_CREATED);
                            //support.executedSaveOnCartDefinitions(request);
                        } else {
                            Spark.halt(HttpURLConnection.HTTP_BAD_REQUEST);
                        }
                        return "";
                    }
            );
            Spark.delete("/:refNo", (request, response) -> {
                        if (cartDefinitions.remove(request.params("refNo"))) {
                            response.status(HttpURLConnection.HTTP_OK);
                            //support.executedDeleteOnCartDefinitions(request);
                        } else {
                            Spark.halt(HttpURLConnection.HTTP_BAD_REQUEST);
                        }
                        return "";
                    }
            );
        });
    }
}

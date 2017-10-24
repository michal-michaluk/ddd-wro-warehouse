package warehouse.carts;

import lombok.AllArgsConstructor;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import warehouse.OpsSupport;
import warehouse.Persistence;
import warehouse.products.CartValidator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by michal on 23.10.2017.
 */
@AllArgsConstructor
public class CartDefinitionRepository {

    private final Sql2o database;
    private final OpsSupport support;

    public List<String> getDefinedRefNos() {
        try (Connection transaction = database.beginTransaction();
             Query query = transaction
                     .createQuery("select refNo from warehouse.CartDefinition")) {
            return query.executeAndFetch(String.class);
        }
    }

    public Optional<String> getJson(String refNo) {
        try (Connection transaction = database.beginTransaction();
             Query query = transaction
                     .createQuery("select definition from warehouse.CartDefinition where refNo = :refNo")) {
            String json = query.addParameter("refNo", refNo)
                    .executeAndFetchFirst(String.class);
            return Optional.ofNullable(json);
        }
    }

    public Optional<CartValidator> getValidator(String refNo) {
        return getJson(refNo)
                .map(json -> new CartValidator(refNo, deserialize(refNo, json)));
    }

    public boolean save(String refNo, String json) {
        if (validate(json)) return false;
        try (Connection transaction = database.beginTransaction();
             Query query = transaction
                     .createQuery("insert into warehouse.CartDefinition (refNo, definition) values (:refNo, :definition)")) {

            query.addParameter("refNo", refNo)
                    .addParameter("definition", json);
            return true;
        }
    }

    public boolean remove(String refNo) {
        try (Connection transaction = database.beginTransaction();
             Query query = transaction
                     .createQuery("delete from warehouse.CartDefinition where refNo = :refNo")) {
            int rows = query.addParameter("refNo", refNo)
                    .executeUpdate().getResult();
            return rows > 0;
        }
    }

    private List<String> deserialize(String refNo, String json) {
        try {
            return Arrays.asList(Persistence.mapper.readValue(json, String[].class));
        } catch (IOException e) {
            support.failedToDeserializeCartDefinition(refNo, json);
            return Collections.emptyList();
        }
    }

    private boolean validate(String json) {
        try {
            List<String> refNos = Arrays.asList(Persistence.mapper.readValue(json, String[].class));
            return !refNos.isEmpty();
        } catch (IOException e) {
            return true;
        }
    }
}

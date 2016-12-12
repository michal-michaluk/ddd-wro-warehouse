package warehouse.products;

import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.sql2o.Sql2o;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.groups.Tuple.tuple;

/**
 * Created by michal on 10.12.2016.
 */
@Ignore
public class Sql2oPlayground {

    @Data
    public static class ProductStockEvent {
        private final long id;
        private final LocalDateTime created;
        private final String refNo;
        private final String type;
        private final String content;
    }

    @Test
    public void sql2oInsert() throws Exception {
        Sql2o sql2o = new Sql2o("jdbc:postgresql://localhost/postgres", "postgres", "");

        ProductStockEvent event = new ProductStockEvent(
                -1, null, "refNi", "NewEntry", "{\"key\":\"value\"}"
        );

        try (org.sql2o.Connection connection = sql2o.beginTransaction()) {
            connection.createQuery(
                    "insert into warehouse.ProductStockHistory(refNo, type, content) " +
                            "values (:refNo, :type, cast(:content AS json))")
                    .addParameter("refNo", event.getRefNo())
                    .addParameter("type", event.getType())
                    .addParameter("content", event.getContent())
                    .executeUpdate();
            connection.commit();
        }

        List<ProductStockEvent> events = sql2o.open()
                .createQuery("select * from warehouse.ProductStockHistory where refNo = :refNo order by id")
                .addParameter("refNo", event.getRefNo())
                .executeAndFetch(ProductStockEvent.class);

        Assertions.assertThat(events)
                .isNotEmpty()
                .extracting(ProductStockEvent::getRefNo, ProductStockEvent::getType, ProductStockEvent::getContent)
                .contains(tuple(event.getRefNo(), event.getType(), event.getContent()));
    }
}

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
import quality.QualityReport;
import quality.QualityReport.Recovered;
import quality.QualityReportService;
import spark.Request;
import spark.Response;
import spark.Spark;
import tools.RequestStream;
import warehouse.Labels;
import warehouse.OpsSupport;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Inbox is temporal API substituting missing external events emitted from other contexts.
 * As long as other context are not fully operational and integration ready.
 * Currently quality assurance employee can post a simple quality report when locking, recovering or destroying storage units.
 * <p>
 * Created by michal on 18.08.2016.
 */
@AllArgsConstructor
public class Inbox {

    public static final ObjectMapper mapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule())
            .registerModule(new JavaTimeModule());

    private final Labels labels;
    private final QualityReportService quality;
    private final OpsSupport support;

    public void exposeApi() {
        Spark.post("/api/v1/inbox/quality/reports", RequestStream.command(RequestStream
                .map((request, response) -> mapper.readTree(request.body()))
                .map(this::parseQualityReport)
                .command(this::processQualityReport)
        ));
    }

    private QualityReport parseQualityReport(Request request, Response response, JsonNode json) {
        return new QualityReport(
                !json.hasNonNull("locked") ? Collections.emptyList() :
                        StreamSupport.stream(json.path("locked").spliterator(), false)
                                .map(JsonNode::asText).map(labels::scanPalette)
                                .collect(Collectors.toList()),
                !json.hasNonNull("recovered") ? Collections.emptyList() :
                        StreamSupport.stream(json.path("recovered").spliterator(), false)
                                .map(unlocked -> new Recovered(
                                        labels.scanPalette(unlocked.path("label").asText()),
                                        unlocked.path("recovered").asInt(),
                                        unlocked.path("scraped").asInt())
                                )
                                .collect(Collectors.toList()),
                !json.hasNonNull("destroyed") ? Collections.emptyList() :
                        StreamSupport.stream(json.path("destroyed").spliterator(), false)
                                .map(JsonNode::asText).map(labels::scanPalette)
                                .collect(Collectors.toList())
        );
    }

    private void processQualityReport(Request request, Response response, QualityReport report) {
        try {
            quality.process(report);
            support.executedCommandOnQualityReportService(request, report);
        } catch (Throwable throwable) {
            support.failedToExecuteCommandOnQualityReportService(request, report, throwable);
            response.status(500);
        }
    }
}

package warehouse;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import tools.SerializationVersioning;
import warehouse.locations.Location;
import warehouse.products.Picked;
import warehouse.products.ReadyToStore;
import warehouse.products.Stored;
import warehouse.quality.Locked;

import static tools.SerializationVersioning.current;
import static tools.SerializationVersioning.obsolete;

/**
 * Created by michal on 15.08.2016.
 */
public class Persistence {

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

    public static final SerializationVersioning serialisation = new SerializationVersioning(
            current(ReadyToStore.class, "ReadyToStore v1", mapper::writeValueAsString, mapper::readValue),
            current(Stored.class, "Stored v1", mapper::writeValueAsString, mapper::readValue),
            current(Picked.class, "Picked v2", mapper::writeValueAsString, mapper::readValue),
            obsolete(Picked.class, "Picked v1", Persistence::pickedFromV1),
            current(Locked.class, "Locked v1", mapper::writeValueAsString, mapper::readValue)
    );

    private static Picked pickedFromV1(String string, Class<Picked> type) throws Throwable {
        JsonNode json = mapper.readTree(string);
        return new Picked(
                new PaletteLabel(
                        json.path("paletteLabel").path("id").asText(),
                        json.path("paletteLabel").path("refNo").asText()
                ),
                json.path("user").asText(),
                new Location(json.path("lastKnownLocation").path("location").asText()),
                Location.onTheMove(json.path("user").asText())
        );
    }

    private Persistence() {
    }
}

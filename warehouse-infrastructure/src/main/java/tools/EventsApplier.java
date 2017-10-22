package tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Created by michal on 20.06.2017.
 */
public class EventsApplier<AGGREGATE> {

    private final Map<Class<?>, BiConsumer<AGGREGATE, ?>> applies = new HashMap<>();
    private BiConsumer<AGGREGATE, Object> missingDefinitionCallback = (aggregate, event) -> {
    };

    public static <AGGREGATE> EventsApplier<AGGREGATE> forType(Class<AGGREGATE> aggregate) {
        return new EventsApplier<>();
    }

    public <EVENT> EventsApplier<AGGREGATE> define(
            Class<EVENT> eventType,
            BiConsumer<AGGREGATE, EVENT> applyFunction) {

        applies.put(eventType, applyFunction);
        return this;
    }

    public EventsApplier<AGGREGATE> onMissingDefinitionSkip() {
        missingDefinitionCallback = (aggregate, event) -> {
        };
        return this;
    }

    public EventsApplier<AGGREGATE> onMissingDefinition(BiConsumer<AGGREGATE, Object> callback) {
        missingDefinitionCallback = callback;
        return this;
    }

    public void apply(AGGREGATE aggregate, List<Object> events) {
        for (Object event : events) {
            BiConsumer<AGGREGATE, Object> func = pickFunction(event);
            func.accept(aggregate, event);
        }
    }

    @SuppressWarnings("unchecked")
    private BiConsumer<AGGREGATE, Object> pickFunction(Object event) {
        return (BiConsumer<AGGREGATE, Object>) applies.getOrDefault(event.getClass(), missingDefinitionCallback);
    }
}

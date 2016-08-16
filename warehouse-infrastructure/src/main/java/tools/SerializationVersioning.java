package tools;

import lombok.Value;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by michal on 11.08.2016.
 */
public class SerializationVersioning {

    @FunctionalInterface
    public interface ToJson<T> {
        String toJson(T object) throws Throwable;
    }

    @FunctionalInterface
    public interface FromJson<T> {
        T fromJson(String json, Class<T> type) throws Throwable;
    }

    @FunctionalInterface
    public interface ToJsonExceptionHandler {
        void onException(Throwable throwable, Object event, Description<?> desc) throws RuntimeException;
    }

    @FunctionalInterface
    public interface FromJsonExceptionHandler {
        void onException(Throwable throwable, String json, String alias, Description<?> desc) throws RuntimeException;
    }

    @Value
    public static class Description<T> {
        private final Class<T> type;
        private final String alias;
        private final ToJson toJson;
        private final FromJson fromJson;
        private final boolean current;
    }

    public static final Rethrow RETHROW = new Rethrow();

    private final Map<String, Description<?>> byAliases;
    private final Map<Class<?>, Description<?>> byEvents;

    private ToJsonExceptionHandler toJsonExceptionHandler = (throwable, event, desc) -> throwable.printStackTrace();
    private FromJsonExceptionHandler fromJsonExceptionHandler = (throwable, json, alias, desc) -> throwable.printStackTrace();

    public static <T> Description<T> current(Class<T> event, String alias, ToJson<T> toJson, FromJson<T> fromJson) {
        return new Description<>(event, alias, toJson, fromJson, true);
    }

    public static <T> Description<T> obsolete(Class<T> event, String alias, FromJson<T> fromJson) {
        return new Description<>(event, alias, null, fromJson, false);
    }

    public SerializationVersioning(Description<?>... descriptions) {
        this.byAliases = Stream.of(descriptions).collect(Collectors.toMap(Description::getAlias, Function.identity(), (desc1, desc2) -> {
            throw new IllegalArgumentException("doubled alias in event serialisation descriptions " + desc1 + " " + desc2);
        }));
        this.byEvents = Stream.of(descriptions)
                .filter(Description::isCurrent)
                .collect(Collectors.toMap(Description::getType, Function.identity(), (desc1, desc2) -> {
                    throw new IllegalArgumentException("two current event serialisation descriptions " + desc1 + " " + desc2);
                }));
    }

    public Description<?> of(String alias) {
        return byAliases.get(alias);
    }

    public Description<?> of(Class<?> eventClass) {
        return byEvents.get(eventClass);
    }

    public SerializationVersioning toJsonExceptionHandler(ToJsonExceptionHandler toJsonExceptionHandler) {
        this.toJsonExceptionHandler = toJsonExceptionHandler;
        return this;
    }

    public SerializationVersioning fromJsonExceptionHandler(FromJsonExceptionHandler fromJsonExceptionHandler) {
        this.fromJsonExceptionHandler = fromJsonExceptionHandler;
        return this;
    }

    public String serialize(Object event) {
        try {
            @SuppressWarnings("unchecked")
            String string = of(event.getClass()).getToJson().toJson(event);
            return string;
        } catch (Throwable throwable) {
            toJsonExceptionHandler.onException(throwable, event, of(event.getClass()));
        }
        return null;
    }

    public Object deserialize(String json, String alias) {
        try {
            Description<?> description = of(alias);
            @SuppressWarnings("unchecked")
            Object event = description.getFromJson().fromJson(json, description.getType());
            return event;
        } catch (Throwable throwable) {
            fromJsonExceptionHandler.onException(throwable, json, alias, of(alias));
        }
        return null;
    }

    public static class Rethrow implements ToJsonExceptionHandler, FromJsonExceptionHandler {

        @Override
        public void onException(Throwable throwable, String json, String alias, Description<?> desc) throws RuntimeException {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                throw new RuntimeException(throwable);
            }
        }

        @Override
        public void onException(Throwable throwable, Object event, Description<?> desc) throws RuntimeException {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                throw new RuntimeException(throwable);
            }
        }
    }
}

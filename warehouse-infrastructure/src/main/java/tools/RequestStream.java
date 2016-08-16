package tools;

import spark.*;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by michal on 09.07.2016.
 */
public class RequestStream {

    private static ExceptionHandler onMappingError = (exception, request, response) -> {
        exception.printStackTrace();
        Spark.halt(HttpURLConnection.HTTP_BAD_REQUEST, exception.toString());
    };

    private static Mapper<Object, Void> onLookupError = (request, response, in) -> {
        Spark.halt(HttpURLConnection.HTTP_NOT_FOUND);
        return null;
    };

    private static Mapper<Object, ?> defaultVariant = (request, response, in) -> {
        Spark.halt(HttpURLConnection.HTTP_BAD_REQUEST);
        return null;
    };

    private static ExceptionHandler onExecutionError = null;

    private static Mapper<Object, ?> onQuerySuccess = (request, response, in) -> {
        response.status(HttpURLConnection.HTTP_OK);
        return in;
    };

    private static Mapper<Void, ?> onCommandSuccess = (request, response, nix) -> {
        response.status(HttpURLConnection.HTTP_ACCEPTED);
        return "";
    };

    public static Route query(Mapper<Void, ?> mapper) {
        return (request, response) -> {
            Object value = mapper.apply(request, response, null);
            return onQuerySuccess.apply(request, response, value);
        };
    }

    public static Route command(Mapper<Void, ?> mapper) {
        return (request, response) -> {
            mapper.apply(request, response, null);
            return onCommandSuccess.apply(request, response, null);
        };
    }

    public static <OUTPUT> Mapper<Void, OUTPUT> map(InitMapper<OUTPUT> map) {
        return (request, response, in) -> map.apply(request, response);
    }

    public static <INPUT, OUTPUT> Mapper<INPUT, OUTPUT> map(Mapper<INPUT, OUTPUT> map) {
        return map;
    }

    @FunctionalInterface
    public interface InitMapper<OUTPUT> {
        OUTPUT apply(Request request, Response response) throws Exception;
    }

    @FunctionalInterface
    public interface TerminalConsumer<AGGREGATE, INPUT> {
        void apply(Request request, Response response, AGGREGATE aggregate, INPUT input) throws Exception;
    }

    @FunctionalInterface
    public interface Mapper<INPUT, OUTPUT> {

        default <NEXTOUTPUT> Mapper<INPUT, NEXTOUTPUT> map(Mapper<OUTPUT, NEXTOUTPUT> map) {
            return (request, response, in) -> {
                OUTPUT output = apply(request, response, in);
                return map.apply(request, response, output);
            };
        }

        default <LOOKEDUP> Lookup<INPUT, OUTPUT, LOOKEDUP> lookupNullable(Mapper<OUTPUT, LOOKEDUP> map) {
            Mapper<OUTPUT, Optional<LOOKEDUP>> optionalMapper = (request, response, in) ->
                    Optional.ofNullable(map.apply(request, response, in));
            return new Lookup<>(this, optionalMapper);
        }

        default <NEXTOUTPUT> Lookup<INPUT, OUTPUT, NEXTOUTPUT> lookup(Mapper<OUTPUT, Optional<NEXTOUTPUT>> map) {
            return new Lookup<>(this, map);
        }

        default <SWITCH> Switch<INPUT, OUTPUT, SWITCH> switchOn(Mapper<OUTPUT, SWITCH> switchOn) {
            return new Switch<>(this, switchOn);
        }

        OUTPUT apply(Request request, Response response, INPUT in) throws Exception;
    }

    @FunctionalInterface
    public interface BiMapper<INPUT1, INPUT2, OUTPUT> {
        OUTPUT apply(Request request, Response response, INPUT1 in1, INPUT2 in2) throws Exception;

        default <NEXTOUTPUT> BiMapper<INPUT1, INPUT2, NEXTOUTPUT> map(Mapper<OUTPUT, NEXTOUTPUT> map) {
            return (request, response, in1, in2) -> {
                OUTPUT output = apply(request, response, in1, in2);
                return map.apply(request, response, output);
            };
        }
    }

    public static class Switch<PREINPUT, INPUT, SWITCH> implements Mapper<PREINPUT, Object> {
        Mapper<PREINPUT, INPUT> composed;
        Mapper<INPUT, SWITCH> switchOn;
        Map<SWITCH, Mapper<INPUT, ?>> variants = new HashMap<>();
        @SuppressWarnings("unchecked")
        Mapper<INPUT, ?> def = (Mapper<INPUT, ?>) defaultVariant;

        protected Switch(Mapper<PREINPUT, INPUT> composed, Mapper<INPUT, SWITCH> switchOn) {
            this.composed = composed;
            this.switchOn = switchOn;
        }

        public <OUTPUT> Switch<PREINPUT, INPUT, SWITCH> when(SWITCH variant, Mapper<INPUT, OUTPUT> mapper) {
            if (variants.containsKey(variant)) {
                throw new IllegalArgumentException("variant '" + variant + "' already defined in SwitchOn: " + variant);
            }
            variants.put(variant, mapper);
            return this;
        }

        public <OUTPUT> Mapper<INPUT, OUTPUT> otherwise(Mapper<INPUT, OUTPUT> mapper) {
            def = mapper;
            return mapper;
        }

        @Override
        public Object apply(Request request, Response response, PREINPUT preinput) throws Exception {
            INPUT input;
            Mapper<INPUT, ?> toExecute;
            try {
                input = composed.apply(request, response, preinput);
                SWITCH switchValue = switchOn.apply(request, response, input);
                toExecute = variants.getOrDefault(switchValue, def);
            } catch (Exception e) {
                onMappingError.handle(e, request, response);
                return null;
            }
            return toExecute.apply(request, response, input);
        }
    }

    public static class Lookup<PREINPUT, INPUT, LOOKEDUP> implements Mapper<PREINPUT, LOOKEDUP> {
        Mapper<PREINPUT, INPUT> composed;
        Mapper<INPUT, Optional<LOOKEDUP>> lookup;

        public Lookup(Mapper<PREINPUT, INPUT> composed, Mapper<INPUT, Optional<LOOKEDUP>> lookup) {
            this.composed = composed;
            this.lookup = lookup;
        }

        public <OUTPUT> Mapper<PREINPUT, OUTPUT> query(BiMapper<LOOKEDUP, INPUT, OUTPUT> execute) {
            return (request, response, in) -> {
                INPUT input;
                Optional<LOOKEDUP> lookedup;
                try {
                    input = composed.apply(request, response, in);
                    lookedup = lookup.apply(request, response, input);
                } catch (Exception e) {
                    onMappingError.handle(e, request, response);
                    return null;
                }
                if (!lookedup.isPresent()) {
                    onLookupError.apply(request, response, input);
                    return null;
                } else {
                    if (onExecutionError == null) {
                        return execute.apply(request, response, lookedup.get(), input);
                    } else {
                        try {
                            return execute.apply(request, response, lookedup.get(), input);
                        } catch (Exception e) {
                            onExecutionError.handle(e, request, response);
                            return null;
                        }
                    }
                }
            };
        }

        public Mapper<PREINPUT, Void> command(TerminalConsumer<LOOKEDUP, INPUT> execute) {
            return (request, response, in) -> {
                INPUT input;
                Optional<LOOKEDUP> lookedup;
                try {
                    input = composed.apply(request, response, in);
                    lookedup = lookup.apply(request, response, input);
                } catch (Exception e) {
                    onMappingError.handle(e, request, response);
                    return null;
                }
                if (!lookedup.isPresent()) {
                    onLookupError.apply(request, response, input);
                } else {
                    if (onExecutionError == null) {
                        execute.apply(request, response, lookedup.get(), input);
                    } else {
                        try {
                            execute.apply(request, response, lookedup.get(), input);
                        } catch (Exception e) {
                            onExecutionError.handle(e, request, response);
                        }
                    }
                }
                return null;
            };
        }

        @Override
        public LOOKEDUP apply(Request request, Response response, PREINPUT preinput) throws Exception {
            INPUT input = composed.apply(request, response, preinput);
            Optional<LOOKEDUP> aggregate = lookup.apply(request, response, input);
            if (aggregate.isPresent()) {
                return aggregate.get();
            }
            return null;
        }
    }

    private RequestStream() {
    }
}

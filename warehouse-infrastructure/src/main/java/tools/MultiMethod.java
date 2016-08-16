package tools;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by michal on 15.08.2016.
 */
public class MultiMethod<T, R> {

    public static final Consumer<Exception> IGNORE = e -> {
    };
    public static final Consumer<Exception> PRINT_STACK_TRACE = Exception::printStackTrace;
    public static final Consumer<Exception> RETHROW = e -> {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    };

    private final Class<R> returned;
    private final String method;
    private final Map<Class<?>, Optional<MethodHandle>> cached = new HashMap<>();

    private MethodHandles.Lookup lookup = MethodHandles.lookup();
    private Consumer<Exception> onMissingHandler = PRINT_STACK_TRACE;

    public static <T> GrabMethod<T> in(Class<T> targetClass) {
        return new GrabMethod<T>(targetClass);
    }

    @SuppressWarnings("unchecked")
    public R call(T object, Object parameter) throws Throwable {
        Optional<MethodHandle> handle = cached.computeIfAbsent(parameter.getClass(),
                type -> find(object, parameter, returned, method));
        if (handle.isPresent()) {
            return (R) handle.get().invoke(object, parameter);
        }
        return null;
    }

    public MultiMethod<T, R> onMissingHandler(Consumer<Exception> onMissingHandler) {
        this.onMissingHandler = onMissingHandler;
        return this;
    }

    private Optional<MethodHandle> find(Object object, Object parameter, Class<?> returned, String method) {
        try {
            return Optional.of(lookup.findVirtual(object.getClass(), method, MethodType.methodType(returned, parameter.getClass())));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            onMissingHandler.accept(e);
            return Optional.empty();
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GrabMethod<T> {
        private final Class<T> targetClass;

        public <R> GrabLookup<T, R> method(Class<R> methodReturnedType, String methodName) {
            return new GrabLookup<>(targetClass, methodReturnedType, methodName);
        }

        public GrabLookup<T, Void> method(String methodName) {
            return new GrabLookup<>(targetClass, void.class, methodName);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GrabLookup<T, R> {
        private final Class<T> targetClass;
        private final Class<R> methodReturnedType;
        private final String methodName;

        public MultiMethod<T, R> lookup(MethodHandles.Lookup lookup) {
            return new MultiMethod<>(targetClass, methodReturnedType, methodName, lookup);
        }
    }

    private MultiMethod(Class<T> forClass, Class<R> returned, String method, MethodHandles.Lookup lookup) {
        this.returned = returned;
        this.method = method;
        this.lookup = lookup;
    }
}

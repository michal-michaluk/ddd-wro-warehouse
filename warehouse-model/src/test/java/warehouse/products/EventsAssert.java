package warehouse.products;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import warehouse.quality.Locked;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 13.07.2016.
 */
public class EventsAssert implements ProductStock.Events {

    private final ProductStock.Events delegate;

    private final List<Object> events = new LinkedList<>();
    private final Map<Class<? extends Object>, Object> last = new HashMap<>();

    public EventsAssert(ProductStock.Events delegate) {
        this.delegate = delegate;
    }

    public EventsAssert() {
        delegate = Mockito.mock(ProductStock.Events.class);
    }

    @Override
    public void emit(ReadyToStore event) {
        delegate.emit(event);
        addEvent(event);
    }

    @Override
    public void emit(Stored event) {
        delegate.emit(event);
        addEvent(event);
    }

    @Override
    public void emit(Picked event) {
        delegate.emit(event);
        addEvent(event);
    }

    @Override
    public void emit(Locked event) {
        delegate.emit(event);
        addEvent(event);
    }

    private void addEvent(Object event) {
        events.add(event);
        last.put(event.getClass(), event);
    }

    @SuppressWarnings("unchecked")
    private <T> AbstractObjectAssert<?, T> assertAt(int index, Class<T> type) {
        AbstractObjectAssert<?, ?> anAssert = Assertions.assertThat(events.get(index));
        anAssert.isInstanceOf(type);
        return (AbstractObjectAssert<?, T>) anAssert;
    }

    public <T> AbstractObjectAssert<?, T> assertFirst(Class<T> type) {
        return this.<T>assertAt(0, type);
    }

    public <T> AbstractObjectAssert<?, T> assertLast(Class<T> type) {
        return this.<T>assertAt(events.size() - 1, type);
    }

    public void notEmitted(Class<?> eventType) {
        Assertions.assertThat(last).doesNotContainKey(eventType);
    }

}

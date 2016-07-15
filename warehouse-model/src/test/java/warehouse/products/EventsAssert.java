package warehouse.products;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;

import java.util.*;

/**
 * Created by michal on 13.07.2016.
 */
public class EventsAssert implements ProductStock.Events {
    List<Object> events = new LinkedList<>();
    Map<Class<? extends Object>, Object> last = new HashMap<>();

    @Override
    public void fire(NewPaletteReadyToStore event) {
        addEvent(event);
    }

    @Override
    public void fire(Stored event) {
        addEvent(event);
    }

    @Override
    public void fire(Picked event) {
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
}

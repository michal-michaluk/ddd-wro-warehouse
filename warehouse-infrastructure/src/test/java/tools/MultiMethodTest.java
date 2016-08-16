package tools;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import warehouse.PaletteLabel;
import warehouse.products.Delivered;
import warehouse.quality.Destroyed;
import warehouse.quality.Locked;
import warehouse.quality.Unlocked;

import java.lang.invoke.MethodHandles;

import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * Created by michal on 15.08.2016.
 */
public class MultiMethodTest {

    public class HandlerOfManyEvents {
        public void handle(Locked event) {
            wasCalled = true;
        }

        protected void handle(Delivered event) {
            wasCalledProtected = true;
        }

        private void handle(Unlocked event) {
            // don't expect it to work
        }

        public void handle(Destroyed event) throws Exception {
            throw new BusinessException();
        }
    }

    public class BusinessException extends Exception {
    }

    private boolean wasCalled = false;
    private boolean wasCalledProtected = false;

    private MultiMethod<HandlerOfManyEvents, Void> handler = MultiMethod
            .in(HandlerOfManyEvents.class).method(void.class, "handle")
            .lookup(MethodHandles.lookup());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void multimethodPublicCall() throws Throwable {
        HandlerOfManyEvents object = new HandlerOfManyEvents();
        handler.call(object, new Locked(null));
        Assertions.assertThat(wasCalled).isTrue();
    }

    @Test
    public void multimethodProtectedCall() throws Throwable {
        HandlerOfManyEvents object = new HandlerOfManyEvents();
        handler.call(object, new Delivered(null));
        Assertions.assertThat(wasCalledProtected).isTrue();
    }

    @Test
    public void silentOnPrivateCall() throws Throwable {
        HandlerOfManyEvents object = new HandlerOfManyEvents();
        handler.call(object, new Unlocked(null));
    }

    @Test
    public void missingHandlerOnPrivateCall() throws Throwable {
        thrown.expectCause(instanceOf(IllegalAccessException.class));
        handler
                .onMissingHandler(e -> {
                    throw new RuntimeException(e);
                });

        HandlerOfManyEvents object = new HandlerOfManyEvents();
        handler.call(object, new Unlocked(null));
    }

    @Test
    public void multimethodThrowingCall() throws Throwable {
        thrown.expect(BusinessException.class);

        HandlerOfManyEvents object = new HandlerOfManyEvents();
        handler.call(object, new Destroyed(null));
    }

    @Test
    public void silentOnMissingCall() throws Throwable {
        HandlerOfManyEvents object = new HandlerOfManyEvents();
        handler.call(object, new PaletteLabel("", ""));
    }

    @Test
    public void missingHandlerOnMissingCall() throws Throwable {
        thrown.expectCause(instanceOf(NoSuchMethodException.class));
        handler
                .onMissingHandler(e -> {
                    throw new RuntimeException(e);
                });

        HandlerOfManyEvents object = new HandlerOfManyEvents();
        handler.call(object, new PaletteLabel("", ""));
    }
}

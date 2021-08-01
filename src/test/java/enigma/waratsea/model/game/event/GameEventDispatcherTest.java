package enigma.waratsea.model.game.event;

import com.google.inject.Guice;
import com.google.inject.Injector;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GameEventDispatcherTest {

    private static Injector injector;

    @BeforeClass
    public static void setup() {
        injector = Guice.createInjector(new TestModule());
    }

    @Test
    public void testEventFireAndReception() {
        TestEvent.init();

        TestEventHandler testEventHandler = injector.getInstance(TestEventHandler.class);
        testEventHandler.register();

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        testEvent.fire();

        Assert.assertTrue(testEventHandler.isEventReceived());

        testEventHandler.setEventReceived(false);

        testEvent.fire();

        Assert.assertTrue(testEventHandler.isEventReceived());
    }

    @Test
    public void testEventFireAndReceptionUnregister() {
        TestEvent.init();

        TestEventHandlerUnregister testEventHandlerUnregister = injector.getInstance(TestEventHandlerUnregister.class);

        testEventHandlerUnregister.register();

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        testEvent.fire();

        Assert.assertTrue(testEventHandlerUnregister.isEventReceived());
    }

    @Test
    public void testTwoEventHandlers() {
        TestEvent.init();

        TestEventHandler testEventHandler = injector.getInstance(TestEventHandler.class);
        TestEventHandler testEventHandler2 = injector.getInstance(TestEventHandler.class);

        testEventHandler.register();
        testEventHandler2.register();

        testEventHandler.setEventReceived(false);
        testEventHandler2.setEventReceived(false);

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        testEvent.fire();

        Assert.assertTrue(testEventHandler.isEventReceived());
        Assert.assertTrue(testEventHandler2.isEventReceived());

        testEventHandler.setEventReceived(false);
        testEventHandler2.setEventReceived(false);

        testEvent.fire();

        Assert.assertTrue(testEventHandler.isEventReceived());
        Assert.assertTrue(testEventHandler2.isEventReceived());
    }

    @Test
    public void testEventFireAndReceptionUnregisterFireAgain() {
        TestEvent.init();

        TestEventHandlerUnregister testEventHandlerUnregister = injector.getInstance(TestEventHandlerUnregister.class);

        testEventHandlerUnregister.register();

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        testEvent.fire();

        Assert.assertTrue(testEventHandlerUnregister.isEventReceived());

        testEventHandlerUnregister.setEventReceived(false);

        testEvent.fire();

        // The event should be ignored
        Assert.assertFalse(testEventHandlerUnregister.isEventReceived());

    }
}

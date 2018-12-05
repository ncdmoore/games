package enigma.waratsea.event;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.event.GameEventRegistry;
import enigma.waratsea.TestModule;
import org.junit.BeforeClass;
import org.junit.Test;

public class GameEventTest {

    private static GameEventRegistry gameEventRegistry;
    private static TestEventHandler testEventHandler;
    private static TestEventHandler testEventHandler2;


    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        gameEventRegistry = injector.getInstance(GameEventRegistry.class);
        testEventHandler = injector.getInstance(TestEventHandler.class);
        testEventHandler2 = injector.getInstance(TestEventHandler.class);
    }

    @Test
    public void testEventFireAndReception() {
        gameEventRegistry.register(TestEvent.class, testEventHandler);

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        gameEventRegistry.fire(testEvent);

        assert (testEventHandler.isEventReceived());
    }

    @Test
    public void testEventFireAndRecptionUnregister() {
        gameEventRegistry.register(TestEvent.class, testEventHandler);

        gameEventRegistry.stopFutureEvents(TestEvent.class, testEventHandler);

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        gameEventRegistry.fire(testEvent);

        assert (testEventHandler.isEventReceived());                                                                    // Must receive at least one event, to stop receiving future events.

        testEventHandler.setEventReceived(false);

        gameEventRegistry.fire(testEvent);

        assert (!testEventHandler.isEventReceived());
    }

    @Test
    public void testTwoEventHandlers() {
        gameEventRegistry.register(TestEvent.class, testEventHandler);
        gameEventRegistry.register(TestEvent.class, testEventHandler2);

        testEventHandler.setEventReceived(false);
        testEventHandler2.setEventReceived(false);

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        gameEventRegistry.fire(testEvent);

        assert (testEventHandler.isEventReceived());
        assert (testEventHandler2.isEventReceived());

    }

}

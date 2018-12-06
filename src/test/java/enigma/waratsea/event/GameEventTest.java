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
    private static TestEventFactory testEventFactory;
    private static TestEventHandlerUnregister testEventHandlerUnregister;


    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        gameEventRegistry = injector.getInstance(GameEventRegistry.class);
        testEventHandler = injector.getInstance(TestEventHandler.class);
        testEventHandler2 = injector.getInstance(TestEventHandler.class);
        testEventFactory = injector.getInstance(TestEventFactory.class);
        testEventHandlerUnregister = injector.getInstance(TestEventHandlerUnregister.class);
    }

    @Test
    public void testEventFireAndReception() {
        gameEventRegistry.register(TestEvent.class, testEventHandler);

        TestEvent testEvent = testEventFactory.create();
        testEvent.setName("A test event");

        testEvent.fire();

        assert (testEventHandler.isEventReceived());
    }

    @Test
    public void testEventFireAndRecptionUnregister() {
        gameEventRegistry.register(TestEvent.class, testEventHandlerUnregister);

        TestEvent testEvent = testEventFactory.create();
        testEvent.setName("A test event");

        testEvent.fire();

        assert (testEventHandlerUnregister.isEventReceived());
    }

    @Test
    public void testTwoEventHandlers() {
        gameEventRegistry.register(TestEvent.class, testEventHandler);
        gameEventRegistry.register(TestEvent.class, testEventHandler2);

        testEventHandler.setEventReceived(false);
        testEventHandler2.setEventReceived(false);

        TestEvent testEvent = testEventFactory.create();
        testEvent.setName("A test event");

        testEvent.fire();

        assert (testEventHandler.isEventReceived());
        assert (testEventHandler2.isEventReceived());

    }

}

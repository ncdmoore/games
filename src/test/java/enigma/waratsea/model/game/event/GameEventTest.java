package enigma.waratsea.model.game.event;

import com.google.inject.Guice;
import com.google.inject.Injector;
import enigma.waratsea.TestModule;
import org.junit.BeforeClass;
import org.junit.Test;

public class GameEventTest {

    private static TestEventHandlerImpl testEventHandler;
    private static TestEventHandlerImpl testEventHandler2;
    private static TestEventHandlerUnregister testEventHandlerUnregister;


    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        testEventHandler = injector.getInstance(TestEventHandlerImpl.class);
        testEventHandler2 = injector.getInstance(TestEventHandlerImpl.class);
        testEventHandlerUnregister = injector.getInstance(TestEventHandlerUnregister.class);
    }

    @Test
    public void testEventFireAndReception() {
        TestEvent.register(testEventHandler);

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        testEvent.fire();

        assert (testEventHandler.isEventReceived());
    }

    @Test
    public void testEventFireAndRecptionUnregister() {
        TestEvent.register(testEventHandlerUnregister);

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        testEvent.fire();

        assert (testEventHandlerUnregister.isEventReceived());
    }

    @Test
    public void testTwoEventHandlers() {
        TestEvent.register(testEventHandler);
        TestEvent.register(testEventHandler2);

        testEventHandler.setEventReceived(false);
        testEventHandler2.setEventReceived(false);

        TestEvent testEvent = new TestEvent();
        testEvent.setName("A test event");

        testEvent.fire();

        assert (testEventHandler.isEventReceived());
        assert (testEventHandler2.isEventReceived());

    }

}

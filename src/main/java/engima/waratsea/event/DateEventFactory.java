package engima.waratsea.event;

/**
 * Date event factory used by guice.
 */
public interface DateEventFactory {

    /**
     * Create a date event.
     * @return A date event.
     */
     DateEvent create();
}

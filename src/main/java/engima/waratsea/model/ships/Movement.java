package engima.waratsea.model.ships;

/**
 * Ship's movement.
 */
public class Movement {

    private final int maxEven;

    private final int maxOdd;

    private int even;

    private int odd;

    /**
     * Constructor.
     *
     * @param maxEven The initial even turn movement health.
     * @param maxOdd The initial odd turn movement health.
     */
    public Movement(final int maxEven, final int maxOdd) {
        this.maxEven = maxEven;
        this.maxOdd = maxOdd;
        this.even = maxEven;
        this.odd = maxOdd;
    }

}

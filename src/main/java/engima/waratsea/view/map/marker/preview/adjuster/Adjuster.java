package engima.waratsea.view.map.marker.preview.adjuster;

/**
 * This class can be used to 'adjust' the x and y coordinates of a marker on the preview game map.
 * This allows for slight differences in the overlaid grid between the preview map and the main map.
 * This allows for an 'adjustment' of the marker on the preview map such that it is in the correct
 * location regarding the preview maps underlying image. This is a nice way to ensure that the preview
 * map and the main map look consistent.
 */
public class Adjuster {
    private final double xAdjustment;
    private final double yAdjustment;

    public Adjuster(final double xAdjustment, final double yAdjustment) {
        this.xAdjustment = xAdjustment;
        this.yAdjustment = yAdjustment;
    }

    /**
     * Adjust the given x-coordinate by the x-adjustment.
     * @param x An x-coordinate.
     * @return The adjusted x-coordinate.
     */
    public double adjustX(final double x) {
        return x + xAdjustment;
    }

    /**
     * Adjust the given y-coordinate by the y-adjustment.
     * @param y A y-coordinate.
     * @return The adjusted y-coordinate.
     */
    public double adjustY(final double y) {
        return y + yAdjustment;
    }
}

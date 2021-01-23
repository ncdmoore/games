package engima.waratsea.view.map.marker.preview.adjuster;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.view.ViewProps;

/**
 * This class provides adjusters for named map locations. Adjusters are used to adjust the location of
 * a map marker that is located at a named location on the preview map. The adjustment is used to align
 * the marker with the underlying map background image. This way the preview map and the main map look
 * in sync even if they differ slightly in grid locations.
 */
@Singleton
public class AdjusterProvider {
    private final ViewProps props;

    @Inject
    public AdjusterProvider(final ViewProps props) {
        this.props = props;
    }

    /**
     * Get an x,y coordinate adjuster for the given marker name.
     * @param name The marker name that requires adjustment on the preview map.
     * @return An x,y coordinate adjuster.
     */
    public Adjuster get(final String name) {
        try {
            double xAdjustment = props.getDouble(name.toLowerCase() + ".x");
            double yAdjustment = props.getDouble(name.toLowerCase() + ".y");
            return new Adjuster(xAdjustment, yAdjustment);
        } catch (Exception ex) {
            return new Adjuster(0.0, 0.0);
        }
    }
}

package engima.waratsea.utility;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Side;
import engima.waratsea.view.ViewProps;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.HashMap;
import java.util.Map;

/**
 * String color name to Paint map.
 */
@Singleton
public class ColorMap {

    private static final Map<String, Paint> MAP = new HashMap<>();

    static {
        MAP.put("Color.BLUE", Color.BLUE);
        MAP.put("Color.GREEN", Color.GREEN);
        MAP.put("Color.RED", Color.RED);
    }

    private static final Map<Side, String> BASE_COLOR_PROPERTY_MAP = new HashMap<>();
    static {
        BASE_COLOR_PROPERTY_MAP.put(Side.ALLIES, "allies.base.color");
        BASE_COLOR_PROPERTY_MAP.put(Side.AXIS, "axis.base.color");
    }

    private ViewProps props;

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public ColorMap(final ViewProps props) {
        this.props = props;
    }

    /**
     * Get the corresponding paint color.
     *
     * @param side the side ALLIES or AXIS.
     * @return The side's base paint color.
     */
    public Paint getBaseColor(final Side side) {
        return MAP.get(props.getString(BASE_COLOR_PROPERTY_MAP.get(side)));
    }

}

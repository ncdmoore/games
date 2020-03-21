package engima.waratsea.model.aircraft.data;

import lombok.Getter;
import lombok.Setter;

/**
 * An aircraft's frame data.
 */
public class FrameData {
    @Getter @Setter private int frame;
    @Getter @Setter private boolean fragile;
}

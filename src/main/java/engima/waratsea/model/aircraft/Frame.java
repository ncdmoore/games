package engima.waratsea.model.aircraft;

import engima.waratsea.model.aircraft.data.FrameData;
import lombok.Getter;

/**
 * Represents the aircraft's frame.
 */
@Getter
public class Frame {
    private final int frame;
    private final boolean fragile;

    /**
     * The constructor.
     *
     * @param data The frame data read in from a JSON file.
     */
    public Frame(final FrameData data) {
        this.frame = data.getFrame();
        this.fragile = data.isFragile();
    }
}

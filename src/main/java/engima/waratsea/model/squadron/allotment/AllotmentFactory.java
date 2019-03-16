package engima.waratsea.model.squadron.allotment;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.allotment.data.AllotmentData;

/**
 * Factory used by guice to create allotments.
 */
public interface AllotmentFactory {
    /**
     * Creates an Allotment.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data Allotment data read from a JSON file.
     * @return An allotment initialized with the data from the JSON file.
     */
    Allotment create(Side side, AllotmentData data);
}

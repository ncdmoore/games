package engima.waratsea.model.base.airfield.patrol.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class PatrolStats {
    @Getter
    @Setter
    private Map<Integer, Map<String, String>> data;

    @Getter
    @Setter
    private Map<String, String> metaData;

    /**
     * Constructor.
     */
    public PatrolStats() {
        metaData = new HashMap<>(); // enuser that the metaData map always exists.
    }
}

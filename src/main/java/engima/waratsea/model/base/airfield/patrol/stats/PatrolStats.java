package engima.waratsea.model.base.airfield.patrol.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class PatrolStats {
    @Getter
    @Setter
    private Map<Integer, Map<String, PatrolStat>> data;

    @Getter
    @Setter
    private Map<String, String> metaData; // Map of patrol radius to squadron names that can reach the radius.

    /**
     * Constructor.
     */
    public PatrolStats() {
        metaData = new HashMap<>();     // ensure that the metaData map always exists.
     }
}

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

    @Getter
    @Setter
    private Map<Integer, String> rowMetaData;

    /**
     * Constructor.
     */
    public PatrolStats() {
        metaData = new HashMap<>();     // ensure that the metaData map always exists.
        rowMetaData = new HashMap<>();  // ensure that the rowMetaData map always exists.
    }
}

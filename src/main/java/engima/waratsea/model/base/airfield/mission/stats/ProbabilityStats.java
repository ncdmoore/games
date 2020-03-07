package engima.waratsea.model.base.airfield.mission.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class ProbabilityStats {
    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String eventColumnTitle;

    @Getter
    @Setter
    private String probabilityColumnTitle = "Probability";

    @Getter
    @Setter
    private Map<Integer, Integer> probability;
}

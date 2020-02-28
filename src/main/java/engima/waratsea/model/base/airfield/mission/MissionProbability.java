package engima.waratsea.model.base.airfield.mission;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class MissionProbability {
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

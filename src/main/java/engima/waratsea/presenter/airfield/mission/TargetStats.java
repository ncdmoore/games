package engima.waratsea.presenter.airfield.mission;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetType;
import lombok.Getter;

public class TargetStats {

    @Getter
    private final String title;

    @Getter
    private final String distance;

    @Getter
    private final String capacitySteps;

    @Getter
    private final String currentSteps;

    @Getter
    private final String routeSteps;

    /**
     * Constructor.
     *
     * @param target The mission target.
     * @param airbase The mission's home airbase.
     * @param routeSteps The number of steps in route to the target.
     */
    public TargetStats(final Target target, final Airbase airbase, final int routeSteps) {
        title = target.getTitle() + " " + TargetType.getTitle(target) + " Details";
        distance = target.getDistance(airbase) + "";
        capacitySteps = target.getCapacitySteps() + "";
        currentSteps = target.getCurrentSteps() + "";
        this.routeSteps = routeSteps + "";
    }
}

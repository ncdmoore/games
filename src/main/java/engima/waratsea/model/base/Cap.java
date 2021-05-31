package engima.waratsea.model.base;

import engima.waratsea.model.base.airfield.mission.MissionSquadrons;

/**
 * Represents combat air patrol (CAP). This may be a patrol originating around an airbase that protects the airbase
 * itself or it may be a mission that provides remote CAP for another task force.
 */
public interface Cap {

    /**
     * This CAP intercepts enemy squadrons.
     *
     * @param enemySquadrons The enemy squadrons that are intercepted.
     */
    void intercept(MissionSquadrons enemySquadrons);
}

package engima.waratsea.view.airfield.mission.stats;

import engima.waratsea.presenter.airfield.mission.MissionStats;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import javafx.scene.Node;

/**
 * Represents the mission statistics view.
 */
public interface StatsView {
    /**
     * Build the stats view.
     *
     * @return The stats view.
     */
    Node build();

    /**
     * Show the mission statistics.
     *
     * @param missionStats The mission statistics.
     */
    void show(MissionStats missionStats);

    /**
     * Add a squadron to the mission stats.
     *
     * @param squadron The squadron added to the mission.
     * @param target The mission's target.
     */
    void addSquadron(Squadron squadron, Target target);

    /**
     * Remove a squadron from the mission stats.
     *
     * @param squadron The squadron removed from the mission.
     * @param target The mission's target.
     */
    void removeSquadron(Squadron squadron, Target target);
}

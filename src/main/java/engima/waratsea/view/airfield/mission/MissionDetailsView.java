package engima.waratsea.view.airfield.mission;

import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.view.util.ListViewPair;

/**
 * Represents the view of the mission details.
 */
public interface MissionDetailsView {
    /**
     * Get the mission list.
     *
     * @param role The squadron mission role.
     * @return The available and assigned mission list view pair.
     */
    ListViewPair<Squadron> getSquadronList(MissionRole role);

    /**
     * Get the target's view.
     *
     * @return The target's view.
     */
    TargetView getTargetView();

    /**
     * Assign a squadron to the mission.
     *
     * @param squadron The assigned squadron.
     * @param role The squadron's mission role.
     */
    void assign(Squadron squadron, MissionRole role);

    /**
     * Remove the currently selected assigned squadron from the mission.
     *
     * @param role The squadron's mission role.
     */
    void remove(MissionRole role);
}

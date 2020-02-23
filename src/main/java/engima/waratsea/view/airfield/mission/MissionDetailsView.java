package engima.waratsea.view.airfield.mission;

import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.view.util.ListViewPair;

/**
 * Represents the view of the mission details.
 */
public interface MissionDetailsView {
    /**
     * Get the mission list.
     *
     * @return The available and assigned mission list view pair.
     */
    ListViewPair<Squadron> getMissionList();

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
     */
    void assign(Squadron squadron);

    /**
     * Remove the currently selected assigned squadron from the mission.
     */
    void remove();
}

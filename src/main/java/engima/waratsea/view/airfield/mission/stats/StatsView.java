package engima.waratsea.view.airfield.mission.stats;

import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
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
    StatsView build();

    /**
     * Get the stats view contents.
     *
     * @return The node containing the stats view.
     */
    Node getContents();

    /**
     * Bind to the view model.
     *
     * @param viewModel The air mission view model.
     * @return  The node containing the view.
     */
    Node bind(AirMissionViewModel viewModel);

}

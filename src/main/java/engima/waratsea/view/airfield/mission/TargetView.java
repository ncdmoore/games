package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.presenter.airfield.mission.MissionStats;
import engima.waratsea.view.airfield.mission.stats.FerryView;
import engima.waratsea.view.airfield.mission.stats.StrikeView;
import engima.waratsea.view.airfield.mission.stats.StatsView;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.Getter;


import java.util.HashMap;
import java.util.Map;


/**
 * Represents the mission's target view details.
 */
public class TargetView {

    private VBox vBox = new VBox();

    @Getter
    private Map<AirMissionType, StatsView> viewMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param ferryViewProvider The ferry view provider.
     * @param strikeViewProvider The land strike view provider.
     */
    @Inject
    public TargetView(final Provider<FerryView> ferryViewProvider,
                      final Provider<StrikeView> strikeViewProvider) {
        viewMap.put(AirMissionType.FERRY, ferryViewProvider.get());
        viewMap.put(AirMissionType.LAND_STRIKE, strikeViewProvider.get());
        viewMap.put(AirMissionType.NAVAL_PORT_STRIKE, strikeViewProvider.get());
        viewMap.put(AirMissionType.SWEEP_AIRFIELD, strikeViewProvider.get());
        viewMap.put(AirMissionType.SWEEP_PORT, strikeViewProvider.get());
    }

    /**
     * Build the target view.
     *
     * @return The target view.
     */
    public Node build() {
        return vBox;
    }

    /**
     * Show the mission statistics.
     *
     * @param missionStats The mission statistics to show.
     */
    public void show(final MissionStats missionStats) {
        vBox.getChildren().clear();
        vBox.getChildren().add(viewMap.get(missionStats.getMissionType()).build());
        viewMap.get(missionStats.getMissionType()).show(missionStats);
    }

}

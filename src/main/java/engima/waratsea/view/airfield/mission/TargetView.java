package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.view.airfield.mission.stats.FerryView;
import engima.waratsea.view.airfield.mission.stats.StatsView;
import engima.waratsea.view.airfield.mission.stats.StrikeView;
import engima.waratsea.viewmodel.AirMissionViewModel;
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
    @Getter private Map<AirMissionType, StatsView> viewMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param ferryViewProvider The ferry view provider.
     * @param strikeViewProvider The land strike view provider.
     */
    @Inject
    public TargetView(final Provider<FerryView> ferryViewProvider,
                      final Provider<StrikeView> strikeViewProvider) {
        viewMap.put(AirMissionType.FERRY, ferryViewProvider.get().build());
        viewMap.put(AirMissionType.LAND_STRIKE, strikeViewProvider.get().build());
        viewMap.put(AirMissionType.NAVAL_PORT_STRIKE, strikeViewProvider.get().build());
        viewMap.put(AirMissionType.NAVAL_TASK_FORCE_STRIKE, strikeViewProvider.get().build());
        viewMap.put(AirMissionType.SWEEP_AIRFIELD, strikeViewProvider.get().build());
        viewMap.put(AirMissionType.SWEEP_PORT, strikeViewProvider.get().build());
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
     * Bind the target view to the view model.
     *
     * @param viewModel the air mission view model.
     * @return The node containing the target view.
     */
    public Node bind(final AirMissionViewModel viewModel) {
        viewModel.getMissionType().addListener((o, ov, nv) -> missionTypeSelected(nv));

        viewMap
                .values()
                .forEach(targetView -> targetView.bind(viewModel));

        return vBox;
    }


    /**
     * Show the mission statistics.
     *
     * @param missionType The mission type to show.
     */
    public void missionTypeSelected(final AirMissionType missionType) {
        vBox
                .getChildren()
                .clear();

        vBox
                .getChildren()
                .add(viewMap.get(missionType).getContents());
    }

}

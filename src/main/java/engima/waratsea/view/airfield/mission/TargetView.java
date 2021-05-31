package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.view.airfield.mission.stats.FerryView;
import engima.waratsea.view.airfield.mission.stats.StatsView;
import engima.waratsea.view.airfield.mission.stats.StrikeView;
import engima.waratsea.view.weather.SmallWeatherView;
import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents the mission's target view details.
 *
 * CSS Styles used.
 *
 * - spacing-15
 */
public class TargetView {
    private final VBox statsVBox = new VBox();
    private final VBox mainVBox = new VBox();
    private final SmallWeatherView weatherView;

    private final Map<AirMissionType, StatsView> viewMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param ferryViewProvider The ferry view provider.
     * @param strikeViewProvider The strike view provider.
     * @param weatherView The weather view.
     */
    @Inject
    public TargetView(final Provider<FerryView> ferryViewProvider,
                      final Provider<StrikeView> strikeViewProvider,
                      final SmallWeatherView weatherView) {
        viewMap.put(AirMissionType.DISTANT_CAP, strikeViewProvider.get().build());
        viewMap.put(AirMissionType.FERRY, ferryViewProvider.get().build());
        viewMap.put(AirMissionType.LAND_STRIKE, strikeViewProvider.get().build());
        viewMap.put(AirMissionType.NAVAL_PORT_STRIKE, strikeViewProvider.get().build());
        viewMap.put(AirMissionType.NAVAL_TASK_FORCE_STRIKE, strikeViewProvider.get().build());
        viewMap.put(AirMissionType.SWEEP_AIRFIELD, strikeViewProvider.get().build());
        viewMap.put(AirMissionType.SWEEP_PORT, strikeViewProvider.get().build());

        this.weatherView = weatherView;
    }

    /**
     * Build the target view.
     *
     * @return The target view.
     */
    public Node build() {
        Node weatherNode = weatherView.build();
        mainVBox.getChildren().addAll(statsVBox, weatherNode);

        mainVBox.getStyleClass().add("spacing-15");

        return mainVBox;
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

        weatherView.bind(viewModel.getIsAffectedByWeather());

        return mainVBox;
    }


    /**
     * Show the mission statistics.
     *
     * @param missionType The mission type to show.
     */
    public void missionTypeSelected(final AirMissionType missionType) {
        statsVBox
                .getChildren()
                .clear();

        statsVBox
                .getChildren()
                .add(viewMap.get(missionType).getContents());
    }

}

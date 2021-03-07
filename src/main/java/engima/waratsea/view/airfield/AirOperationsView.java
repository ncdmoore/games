package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import engima.waratsea.view.airfield.mission.stats.ProbabilityStatsView;
import engima.waratsea.view.weather.SmallWeatherView;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

/**
 * The air operations view.
 *
 * CSS styles used.
 *
 *  - spacing-10
 *  - title-pane-non-collapsible
 */
public class AirOperationsView {
    private final TitledPane titledPane = new TitledPane();
    private final ProbabilityStatsView statsView;
    private final SmallWeatherView weatherView;

    @Inject
    public AirOperationsView(final ProbabilityStatsView statsView,
                             final SmallWeatherView weatherView) {
        this.statsView = statsView;
        this.weatherView = weatherView;

    }

    /**
     * Build the air operations view.
     *
     * @return This air operations view.
     */
    public AirOperationsView build() {
        titledPane.getStyleClass().add("title-pane-non-collapsible");
        titledPane.setText("Air Operations");
        return this;
    }

    /**
     * Bind the air operations view to the given view model.
     * The air operations are fixed per airbase, so they do
     * not change.
     *
     * @param viewModel The airbase view model for a nation.
     * @return The node containing the air operations view.
     */
    public Node bind(final NationAirbaseViewModel viewModel) {
        Node statsNode = statsView.build(viewModel.getAirOperationStats());
        Node weatherNode = weatherView.build();

        weatherView.bind(viewModel.getAirOperationsAffectedByWeather());

        VBox vBox = new VBox(titledPane, statsNode, weatherNode);
        vBox.getStyleClass().add("spacing-10");
        return vBox;
    }
}

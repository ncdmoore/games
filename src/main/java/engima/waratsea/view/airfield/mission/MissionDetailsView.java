package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.mission.stats.ProbabilityStatsView;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the mission details view. Displayed when a mission arrow is clicked on the game map.
 */
public class MissionDetailsView {
    private final ViewProps props;

    @Getter private final Map<PatrolType, List<Label>> labelMap = new HashMap<>();
    @Getter private final TabPane missionsTabPane = new TabPane();

    private final ProbabilityStatsView statsView;

    /**
     * Constructor called by guice.
     *
     * @param statsView The mission statistics view.
     * @param props The view properties.
     */
    @Inject
    public MissionDetailsView(final ProbabilityStatsView statsView,
                              final ViewProps props) {
        this.statsView = statsView;
        this.props = props;

        Stream.of(PatrolType.values())
                .forEach(patrolType -> labelMap.put(patrolType, new ArrayList<>()));
    }

    /**
     * Show the airfield details.
     *
     * @param missions The airfield's missions that are represented by the arrow that was clicked.
     * @return A node containing the mission details.
     */
    public Node show(final List<AirMission> missions) {
        missionsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<Tab> tabs = missions
                .stream()
                .map(this::buildTab)
                .collect(Collectors.toList());

        missionsTabPane.getTabs().addAll(tabs);

        return missionsTabPane;
    }

    /**
     * Build a tab for a type of air mission.
     *
     * @param mission A given air mission.
     * @return A tab for the given mission.
     */
    private Tab buildTab(final AirMission mission) {
        Tab tab = new Tab(mission.getType().getTitle() + ":" + mission.getTarget().getTitle());
        Node statsNode = statsView.build(mission.getMissionProbability());
        tab.setContent(statsNode);
        return tab;
    }
}

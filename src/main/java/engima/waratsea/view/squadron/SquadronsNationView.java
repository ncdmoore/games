package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class contains the squadron information for a given nation.
 */
public class SquadronsNationView {
    @Getter private Map<SquadronViewType, SquadronsTypeView> squadronsTypeView = new HashMap<>();
    private final Provider<SquadronsTypeView> squadronsTypeViewProvider;

    private Nation nation;              // The nation: BRITISH, ITALIAN, etc...
    private List<Squadron> squadrons;   // The squadrons for the above nation.

    /**
     * Constructor called by guice.
     *
     * @param squadronsTypeViewProvider Provides squadron view.
     */
    @Inject
    public SquadronsNationView(final Provider<SquadronsTypeView> squadronsTypeViewProvider) {
        this.squadronsTypeViewProvider = squadronsTypeViewProvider;
    }

    /**
     * Show the squadron's view.
     *
     * @param squadronNation The nation.
     * @param squadronList The nation's squadrons.
     * @return a node that contains the squadron's view.
     */
    public Node build(final Nation squadronNation, final List<Squadron> squadronList) {
        nation = squadronNation;
        squadrons = squadronList;

        TabPane squadronViewTabs = new TabPane();
        squadronViewTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Stream.of(SquadronViewType.values())
                .sorted()
                .map(this::createTab)
                .forEach(tab -> squadronViewTabs.getTabs().add(tab));

        VBox vBox = new VBox(squadronViewTabs);
        vBox.setId("nation-vbox");
        return vBox;
    }

    /**
     * Create a squadron view type tab.
     *
     * @param squadronViewType The type of squadron in the view.
     * @return A squadron view tab.
     */
    private Tab createTab(final SquadronViewType squadronViewType) {
        Tab tab = new Tab(squadronViewType.toString());

        tab.setUserData(squadronViewType);

        List<Squadron> squadronsOfNeededType = squadrons
                .stream()
                .filter(squadron -> SquadronViewType.get(squadron.getType()) == squadronViewType)
                .collect(Collectors.toList());

        SquadronsTypeView squadronView = squadronsTypeViewProvider.get();
        squadronsTypeView.put(squadronViewType, squadronView);

        Node squadron = squadronView.build(nation, squadronViewType, squadronsOfNeededType);

        boolean disabled = squadronsOfNeededType.isEmpty();

        tab.setDisable(disabled);
        tab.setContent(squadron);
        return tab;
    }


}

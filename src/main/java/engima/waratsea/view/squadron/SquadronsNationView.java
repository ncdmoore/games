package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.viewmodel.squadrons.NationSquadronsViewModel;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class contains the squadron information for a given nation.
 */
public class SquadronsNationView {
    @Getter private final Map<SquadronViewType, SquadronsTypeView> squadronsTypeView = new HashMap<>();
    private final Provider<SquadronsTypeView> squadronsTypeViewProvider;

    private NationSquadronsViewModel viewModel;

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
     * @param vm The view model.
     * @return a node that contains the squadron's view.
     */
    public Node buildAndBind(final NationSquadronsViewModel vm) {
        viewModel = vm;

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

        SquadronsTypeView squadronView = squadronsTypeViewProvider.get();
        squadronsTypeView.put(squadronViewType, squadronView);

        Node squadron = squadronView.buildAndBind(viewModel.getType(squadronViewType));

        boolean disabled = viewModel
                .getType(squadronViewType)
                .getSquadrons()
                .getValue()
                .isEmpty();

        tab.setUserData(squadronViewType);
        tab.setDisable(disabled);
        tab.setContent(squadron);

        return tab;
    }


}

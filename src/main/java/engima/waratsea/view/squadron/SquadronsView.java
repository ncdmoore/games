package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.squadrons.SideSquadronsViewModel;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The squadron view used by the forces menu.
 */
public class SquadronsView {
    private static final String ROUNDEL = ".roundel.image";
    @Getter private final Map<Nation, SquadronsNationView> nationView = new HashMap<>();

    private final Provider<SquadronsNationView> squadronsNationViewProvider;
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private SideSquadronsViewModel viewModel;

    /**
     * Constructor called by guice.
     *
     * @param squadronsNationViewProvider The nation's squadron view.
     * @param imageResourceProvider Provides images.
     * @param props The view properties.
     */
    @Inject
    public SquadronsView(final Provider<SquadronsNationView> squadronsNationViewProvider,
                         final ImageResourceProvider imageResourceProvider,
                         final ViewProps props) {
        this.squadronsNationViewProvider = squadronsNationViewProvider;
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
    }

    /**
     * Show the squadron's view.
     *
     * @param vm The view model.
     * @return a node that contains the squadron's view.
     */
    public Node buildAndBind(final SideSquadronsViewModel vm) {
        viewModel = vm;

        TabPane nationViewTabs = new TabPane();
        nationViewTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<Tab> tabs = viewModel
                .getNations()
                .stream()
                .map(this::createTab)
                .collect(Collectors.toList());

        nationViewTabs.getTabs().addAll(tabs);

        return nationViewTabs;
    }

    /**
     * Get the tabs.
     *
     * @param nation The nation.
     * @return the tabs.
     */
    public Map<SquadronViewType, SquadronsTypeView> getSquadronTypeTabs(final Nation nation) {
        return nationView.get(nation).getSquadronsTypeView();
    }

    /**
     * Create a nation's squadron's tab.
     *
     * @param nation The nation.
     * @return The nation's tab.
     */
    private Tab createTab(final Nation nation) {
        Tab tab = new Tab(nation.toString());

        SquadronsNationView nationSquadronsView = squadronsNationViewProvider.get();
        nationView.put(nation, nationSquadronsView);
        Node content = nationSquadronsView.buildAndBind(viewModel.getNationViewModel(nation));
        ImageView roundel = imageResourceProvider.getImageView(props.getString(nation + ROUNDEL));

        tab.setGraphic(roundel);
        tab.setContent(content);
        tab.setUserData(nation);

        return tab;
    }

}

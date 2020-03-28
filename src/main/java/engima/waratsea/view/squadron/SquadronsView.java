package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.player.Player;
import engima.waratsea.utility.ImageResourceProvider;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SquadronsView {
    private static final String ROUNDEL_SIZE = "20x20.png";
    @Getter private Map<Nation, SquadronsNationView> nationView = new HashMap<>();

    private final Provider<SquadronsNationView> squadronsNationViewProvider;
    private final ImageResourceProvider imageResourceProvider;

    private Player player;

    /**
     * Constructor called by guice.
     *
     * @param squadronsNationViewProvider The nation's squadron view.
     * @param imageResourceProvider Provides images.
     */
    @Inject
    public SquadronsView(final Provider<SquadronsNationView> squadronsNationViewProvider,
                         final ImageResourceProvider imageResourceProvider) {
        this.squadronsNationViewProvider = squadronsNationViewProvider;
        this.imageResourceProvider = imageResourceProvider;
    }

    /**
     * Show the squadron's view.
     *
     * @param humanPlayer The human player.
     * @return a node that contains the squadron's view.
     */
    public Node show(final Player humanPlayer) {
        player = humanPlayer;

        TabPane nationViewTabs = new TabPane();
        nationViewTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<Tab> tabs = player
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

        tab.setUserData(nation);

        SquadronsNationView nationSquadronsView = squadronsNationViewProvider.get();
        nationView.put(nation, nationSquadronsView);
        Node content = nationSquadronsView.build(nation, player.getSquadrons(nation));
        ImageView roundel = imageResourceProvider.getImageView(nation + ROUNDEL_SIZE);
        tab.setGraphic(roundel);

        tab.setContent(content);
        return tab;
    }

}
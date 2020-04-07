package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.presenter.airfield.AirfieldDialog;
import engima.waratsea.presenter.airfield.patrol.PatrolDialog;
import engima.waratsea.view.MainMenu;
import engima.waratsea.view.asset.AirfieldAssetSummaryView;
import engima.waratsea.view.asset.AssetId;
import engima.waratsea.view.asset.AssetSummaryView;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.view.map.marker.main.BaseMarker;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * This class handles all interaction with the main game map.
 */
@Slf4j
@Singleton
public class MainMapPresenter {

    private Game game;
    private MainMapView mainMapView;
    private MainMenu mainMenu;

    private Provider<AirfieldDialog> airfieldDetailsDialogProvider;
    private Provider<PatrolDialog> patrolDetailsDialogProvider;
    private Provider<AssetSummaryView> assetSummaryViewProvider;
    private Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider;

    /**
     * The constructor called by guice.
     *
     * @param game The game.
     * @param viewProvider Provides the main map view.
     * @param menuProvider Provides the main menu.
     * @param airfieldDetailsDialogProvider Provides airfield details dialog.
     * @param patrolDetailsDialogProvider Provides patrol radius details dialog.
     * @param assetSummaryViewProvider Provides the asset summary view.
     * @param airfieldAssetSummaryViewProvider Provides the airfield asset summary view.
     */
    @Inject
    public MainMapPresenter(final Game game,
                            final Provider<MainMapView> viewProvider,
                            final Provider<MainMenu> menuProvider,
                            final Provider<AirfieldDialog> airfieldDetailsDialogProvider,
                            final Provider<PatrolDialog> patrolDetailsDialogProvider,
                            final Provider<AssetSummaryView> assetSummaryViewProvider,
                            final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider) {
        this.game = game;

        mainMapView = viewProvider.get();
        mainMenu = menuProvider.get();

        this.airfieldDetailsDialogProvider = airfieldDetailsDialogProvider;
        this.patrolDetailsDialogProvider = patrolDetailsDialogProvider;
        this.assetSummaryViewProvider = assetSummaryViewProvider;
        this.airfieldAssetSummaryViewProvider = airfieldAssetSummaryViewProvider;
    }

    /**
     * Setup mouse event handlers for when the base grids are clicked.
     */
    public void setBaseClickHandler() {
        mainMenu.getShowAirfields().setOnAction(event -> toggleMarkers());
        mainMenu.getShowPorts().setOnAction(event -> toggleMarkers());

        Side humanSide =  game.getHumanSide();
        mainMapView.setBaseClickHandler(humanSide, this::humanBaseClickHandler);
        mainMapView.setBaseClickHandler(humanSide.opposite(), this::computerBaseClickHandler);

        mainMapView.setPatrolRadiusClickHandler(humanSide, this::patrolRadiusClickHandler);

        mainMapView.setAirfieldMenuHandler(humanSide, this::airfieldHandler);
    }

    /**
     * Callback for when the show airfields map menu item is clicked.
     */
    private void toggleMarkers() {
        mainMapView.toggleBaseMarkers(Side.ALLIES);
        mainMapView.toggleBaseMarkers(Side.AXIS);
    }

    /**
     * Callback for when a human base grid is clicked.
     *
     * @param event The mouse event.
     */
    private void humanBaseClickHandler(final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            VBox imageView = (VBox) event.getSource();
            BaseMarker baseMarker = (BaseMarker) imageView.getUserData();
            boolean selected = mainMapView.selectMarker(baseMarker);

            if (selected) {
                addToAssetSummary(baseMarker);
            } else {
                removeFromAssetSummary(baseMarker);
            }
        }
    }

    /**
     * Callback for when the a computer base grid is clicked.
     *
     * @param event The mouse event.
     */
    private void computerBaseClickHandler(final MouseEvent event) {
        VBox imageView = (VBox) event.getSource();

        BaseMarker baseMarker = (BaseMarker) imageView.getUserData();

        String portName = baseMarker.getBaseGrid().getPort().map(Port::getName).orElse("");
        String airfieldName = baseMarker.getBaseGrid().getAirfield().map(Airfield::getName).orElse("");

        log.info("Computer: Base port: '{}', airfield: '{}'", portName, airfieldName);
    }

    /**
     * Callback for when the airfield menu item of the base marker's context menu is selected.
     *
     * @param event The click event.
     */
    @SuppressWarnings("unchecked")
    private void airfieldHandler(final ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        Optional<Airfield> airfield = (Optional<Airfield>) item.getUserData();
        airfield.ifPresent(a -> airfieldDetailsDialogProvider.get().show(a));
    }

    /**
     * Callback for when an airfield's patrol radius is clicked.
     *
     * @param event The click event.
     */
    @SuppressWarnings("unchecked")
    private void patrolRadiusClickHandler(final MouseEvent event) {
        Circle circle = (Circle) event.getSource();
        List<Patrol> patrols = (List<Patrol>) circle.getUserData();
        patrolDetailsDialogProvider.get().show(patrols);
    }

    /**
     * Add the base's airfield to the asset summary if it exists for the given marker.
     *
     * @param baseMarker The base marker that was clicked.
     */
    private void addToAssetSummary(final BaseMarker baseMarker) {
        baseMarker
                .getBaseGrid()
                .getAirfield()
                .ifPresent(this::addAirfieldToAssetSummary);
    }

    /**
     * Add an airfield to the asset summary.
     *
     * @param airfield The airfield to add.
     */
    private void addAirfieldToAssetSummary(final Airfield airfield) {
        AirfieldAssetSummaryView assetView = airfieldAssetSummaryViewProvider.get();
        assetView.build();
        assetView.show(airfield);
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airfield.getTitle());
        assetSummaryViewProvider.get().show(assetId, assetView);
    }

    /**
     * Remove the base's airfield from the asset summary if it exists for the given marker.
     *
     * @param baseMarker The base marker that was clicked.
     */
    private void removeFromAssetSummary(final BaseMarker baseMarker) {
        baseMarker
                .getBaseGrid()
                .getAirfield()
                .ifPresent(this::removeAirfieldFromAssetSummary);
    }

    /**
     * Remove an airfield from the asset summary.
     *
     * @param airfield The airfield to remove.
     */
    private void removeAirfieldFromAssetSummary(final Airfield airfield) {
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airfield.getTitle());
        assetSummaryViewProvider.get().hide(assetId);
    }
}

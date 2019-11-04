package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.presenter.airfield.AirfieldDetailsDialog;
import engima.waratsea.view.MainMenu;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.view.map.marker.main.BaseMarker;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

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

    private Provider<AirfieldDetailsDialog> airfieldDetailsDialogProvider;

    /**
     * The constructor called by guice.
     *
     * @param game The game.
     * @param viewProvider Provides the main map view.
     * @param menuProvider Provides the main menu.
     * @param airfieldDetailsDialogProvider Provides airfield details dialog.
     */
    @Inject
    public MainMapPresenter(final Game game,
                            final Provider<MainMapView> viewProvider,
                            final Provider<MainMenu> menuProvider,
                            final Provider<AirfieldDetailsDialog> airfieldDetailsDialogProvider) {
        this.game = game;

        mainMapView = viewProvider.get();
        mainMenu = menuProvider.get();

        this.airfieldDetailsDialogProvider = airfieldDetailsDialogProvider;
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
            ImageView imageView = (ImageView) event.getSource();
            BaseMarker baseMarker = (BaseMarker) imageView.getUserData();
            mainMapView.selectMarker(baseMarker);
        }
    }

    /**
     * Callback for when the a computer base grid is clicked.
     *
     * @param event The mouse event.
     */
    private void computerBaseClickHandler(final MouseEvent event) {
        ImageView imageView = (ImageView) event.getSource();

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
}

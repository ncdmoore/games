package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.view.map.marker.main.BaseMarker;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * This class handles all interaction with the main game map.
 */
@Slf4j
@Singleton
public class MainMapPresenter {

    private Game game;

    /**
     * The constructor called by guice.
     *
     * @param game The game.
     */
    @Inject
    public MainMapPresenter(final Game game) {
        this.game = game;
    }

    @Setter
    private MainMapView mainMapView;

    /**
     * Setup mouse event handlers for when the base grids are clicked.
     */
    public void setBaseClickHandler() {
        Side humanSide =  game.getHumanPlayer().getSide();
        mainMapView.setBaseClickHandler(humanSide, this::humanBaseClickHandler);
        mainMapView.setBaseClickHandler(humanSide.opposite(), this::computerBaseClickHandler);
    }

    /**
     * Callback for when a human base grid is clicked.
     *
     * @param event The mouse event.
     */
    private void humanBaseClickHandler(final MouseEvent event) {
        ImageView imageView = (ImageView) event.getSource();

        BaseMarker baseMarker = (BaseMarker) imageView.getUserData();

        String portName = Optional.ofNullable(baseMarker.getBaseGrid().getPort()).map(Port::getName).orElse("");
        String airfieldName = Optional.ofNullable(baseMarker.getBaseGrid().getAirfield()).map(Airfield::getName).orElse("");

        log.info("Human: Base port: '{}', airfield: '{}'", portName, airfieldName);
    }

    /**
     * Callback for when the a computer base grid is clicked.
     *
     * @param event The mouse event.
     */
    private void computerBaseClickHandler(final MouseEvent event) {
        ImageView imageView = (ImageView) event.getSource();

        BaseMarker baseMarker = (BaseMarker) imageView.getUserData();

        String portName = Optional.ofNullable(baseMarker.getBaseGrid().getPort()).map(Port::getName).orElse("");
        String airfieldName = Optional.ofNullable(baseMarker.getBaseGrid().getAirfield()).map(Airfield::getName).orElse("");

        log.info("Computer: Base port: '{}', airfield: '{}'", portName, airfieldName);
    }
}

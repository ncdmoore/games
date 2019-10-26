package engima.waratsea.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.view.turn.TurnView;
import engima.waratsea.view.weather.WeatherView;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


/**
 * The main game window's view.
 */
@Slf4j
@Singleton
public class MainView {
    private static final String CSS_FILE = "mainView.css";

    private CssResourceProvider cssResourceProvider;
    private MainMapView mainMapView;
    private MainMenu mainMenu;
    private WeatherView weatherView;
    private TurnView turnView;

    /**
     * Constructor called by guice.
     * @param cssResourceProvider Utility to provide css files.
     * @param mainMapView The main game map view.
     * @param mainMenu The main game menu.
     * @param weatherView The weather view.
     * @param turnView The turn view.
     */
    @Inject
    public MainView(final CssResourceProvider cssResourceProvider,
                    final MainMapView mainMapView,
                    final MainMenu mainMenu,
                    final WeatherView weatherView,
                    final TurnView turnView) {
        this.cssResourceProvider = cssResourceProvider;
        this.mainMapView = mainMapView;
        this.mainMenu = mainMenu;
        this.weatherView = weatherView;
        this.turnView = turnView;
    }

    /**
     * Show the task forces summary view.
     * @param stage The stage on which the task force scene is set.
     */
    public void show(final Stage stage) {

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());

        BorderPane mainPane = new BorderPane();

        MenuBar menuBar = mainMenu.getMenuBar();
        Node map = mainMapView.getMap();

        Node weather = weatherView.build();
        Node turn = turnView.build();

        VBox leftVbox = new VBox(weather, turn);

        VBox mapVbox = new VBox(map);

        ScrollPane sp = new ScrollPane();
        sp.setContent(mapVbox);
        sp.setFitToWidth(true);

        mainPane.setTop(menuBar);
        mainPane.setLeft(leftVbox);
        mainPane.setCenter(sp);

        Scene scene = new Scene(mainPane, primaryScreenBounds.getWidth(), primaryScreenBounds.getHeight());

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }



}

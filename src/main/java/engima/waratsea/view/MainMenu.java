package engima.waratsea.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import lombok.Getter;

/**
 * The game's main menu.
 */
@Singleton
public class MainMenu {

    @Getter private final MenuItem save;
    @Getter private final MenuItem exitMain;
    @Getter private final MenuItem exitGame;

    @Getter private final CheckMenuItem showAirfields;
    @Getter private final CheckMenuItem showPorts;
    @Getter private final CheckMenuItem showRegions;

    @Getter private final MenuItem airfieldSquadrons;
    @Getter private final MenuItem taskForceSquadrons;

    @Getter private final MenuItem victoryConditions;

    @Getter private final MenuBar menuBar = new MenuBar();

    /**
     * Constructor called by guice.
     */
    @Inject
    public MainMenu() {
        Menu menuFile = new Menu("File");
        save = new MenuItem("_Save");
        exitMain = new MenuItem("Exit to Main Menu");
        exitGame = new MenuItem("_Exit Game");
        menuFile.getItems().addAll(save, exitMain, exitGame);

        Menu menuMap = new Menu("Map");
        showAirfields = new CheckMenuItem("Show _Airfields");
        showAirfields.setSelected(true);
        showPorts = new CheckMenuItem("Show _Ports");
        showPorts.setSelected(true);
        showRegions = new CheckMenuItem("Show _Regions");
        showRegions.setSelected(false);
        menuMap.getItems().addAll(showAirfields, showPorts, showRegions);

        Menu menuOOB = new Menu("Forces");
        airfieldSquadrons = new MenuItem("_Airfield Squadrons");
        taskForceSquadrons = new MenuItem("_Task Force Squadrons");
        menuOOB.getItems().addAll(airfieldSquadrons, taskForceSquadrons);

        Menu menuVictory = new Menu("Victory");
        victoryConditions = new MenuItem("_Victory Conditions");
        menuVictory.getItems().add(victoryConditions);

        menuBar.getMenus().addAll(menuFile, menuMap, menuOOB, menuVictory);
        menuBar.setUseSystemMenuBar(true);                                    // Menu will appear in mac system menu area like all other mac applications.
    }
}

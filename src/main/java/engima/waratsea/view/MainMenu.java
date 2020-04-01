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

    @Getter private MenuItem save;
    @Getter private MenuItem exitMain;
    @Getter private MenuItem exitGame;

    @Getter private CheckMenuItem showAirfields;
    @Getter private CheckMenuItem showPorts;

    @Getter private MenuItem airfieldSquadrons;
    @Getter private MenuItem taskForceSquadrons;

    @Getter private MenuBar menuBar = new MenuBar();

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
        menuMap.getItems().addAll(showAirfields, showPorts);

        Menu menuOOB = new Menu("Forces");
        airfieldSquadrons = new MenuItem("_Airfield Squadrons");
        taskForceSquadrons = new MenuItem("_Task Force Squadrons");
        menuOOB.getItems().addAll(airfieldSquadrons, taskForceSquadrons);

        menuBar.getMenus().addAll(menuFile, menuMap, menuOOB);
        menuBar.setUseSystemMenuBar(true);                          // Menu will appear in mac system menu area like all other mac applications.
    }
}

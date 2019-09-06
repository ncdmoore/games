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

    @Getter
    private MenuItem save;

    @Getter
    private MenuItem quit;

    @Getter
    private CheckMenuItem showAirfields;

    @Getter
    private CheckMenuItem showPorts;

    @Getter
    private MenuBar menuBar = new MenuBar();

    /**
     * Constructor called by guice.
     */
    @Inject
    public MainMenu() {
        Menu menuFile = new Menu("File");
        save = new MenuItem("_Save");
        quit = new MenuItem("_Quit");

        menuFile.getItems().addAll(save, quit);

        Menu menuMap = new Menu("Map");
        showAirfields = new CheckMenuItem("Show _Airfields");
        showAirfields.setSelected(true);
        showPorts = new CheckMenuItem("Show _Ports");
        showPorts.setSelected(true);

        menuMap.getItems().addAll(showAirfields, showPorts);

        menuBar.getMenus().addAll(menuFile, menuMap);
    }
}

package engima.waratsea.view;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import lombok.Getter;

/**
 * The game's main menu.
 */
public class MainMenu {

    @Getter
    private MenuItem quit;

    /**
     * Build the main menu bar.
     *
     * @return The main menu bar.
     */
    public MenuBar build() {
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        quit = new MenuItem("_Quit");
        menuFile.getItems().add(quit);

        menuBar.getMenus().add(menuFile);

        return menuBar;
    }
}

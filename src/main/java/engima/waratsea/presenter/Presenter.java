package engima.waratsea.presenter;

import javafx.stage.Stage;

/**
 * The presenter interface. Every GUI screen component has a presenter and should implement this interface.
 */
public interface Presenter {
    /**
     * Show the primary stage.
     *
     * @param primaryStage The primary javafx stage.
     */
    void show(Stage primaryStage);

    /**
     * Re show the primary stage.
     *
     * @param primaryStage The primary javafx stage.
     */
    void reShow(Stage primaryStage);
}

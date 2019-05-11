package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.view.MainView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the main presenter for the game.
 */
@Slf4j
public class MainPresenter implements Presenter {

    private Provider<MainView> viewProvider;
    private MainView view;
    private Stage stage;

    /**
     * Constructor called by guice.
     * @param viewProvider The view provider.
     */
    @Inject
    public MainPresenter(final Provider<MainView> viewProvider) {
        this.viewProvider = viewProvider;
    }

    /**
     * Show the main game view.
     * @param primaryStage The primary Javafx stage.
     */
    public void show(final Stage primaryStage) {
        log.info("show.");

        view = viewProvider.get();

        this.stage = primaryStage;

        view.show(stage);
    }
}

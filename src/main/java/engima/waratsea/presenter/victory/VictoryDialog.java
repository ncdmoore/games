package engima.waratsea.presenter.victory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.victory.VictoryType;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogOkOnlyView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.victory.VictoryView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.stream.Stream;

public class VictoryDialog {
    private static final String CSS_FILE = "victoryDetails.css";

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogOkOnlyView> dialogProvider;
    private final Provider<VictoryView> viewProvider;
    private final ViewProps props;

    private Stage stage;

    @Inject
    public VictoryDialog(final CssResourceProvider cssResourceProvider,
                         final Provider<DialogOkOnlyView> dialogProvider,
                         final Provider<VictoryView> viewProvider,
                         final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.props = props;
    }

    /**
     * Show the victory details dialog.
     */
    public void show() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Victory Details");

        DialogOkOnlyView dialog = dialogProvider.get();
        VictoryView view = viewProvider.get();

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));

        dialog.setContents(view.build());

        Stream
                .of(VictoryType.values())
                .forEach(type -> view
                        .getVictoryConditions()
                        .get(type)
                        .getSelectionModel()
                        .selectFirst());

        dialog.getOkButton().setOnAction(event -> ok());

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Close this dialog.
     */
    private void ok() {
        stage.close();
    }
}

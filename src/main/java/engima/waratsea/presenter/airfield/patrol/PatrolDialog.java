package engima.waratsea.presenter.airfield.patrol;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.AirbaseGroup;
import engima.waratsea.model.taskForce.patrol.PatrolGroup;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogOkOnlyView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.patrol.PatrolDetailsView;
import engima.waratsea.view.map.MainMapView;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Controls the patrol details dialog shown when a patrol radius circle is clicked on the main game map.
 */
@Slf4j
public class PatrolDialog {
    private static final String CSS_FILE = "patrolDetails.css";
    private static final int SHADOW_RADIUS = 3;

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogOkOnlyView> dialogProvider;
    private final Provider<PatrolDetailsView> viewProvider;
    private final Provider<MainMapView> mapViewProvider;

    private final ViewProps props;

    private DialogOkOnlyView dialog;

    private Stage stage;

    private AirbaseGroup airbaseGroup;

    private PatrolDetailsView view;

    private Label highlighted; // The current selected patrol radius label.

    /**
     * Constructor called by guice.
     *
     * @param cssResourceProvider Provides CSS file.
     * @param dialogProvider Provides the dialog view.
     * @param viewProvider Provides the patrol view.
     * @param mapViewProvider Provides the main map view.
     * @param props The view property.
     */
    @Inject
    public PatrolDialog(final CssResourceProvider cssResourceProvider,
                        final Provider<DialogOkOnlyView> dialogProvider,
                        final Provider<PatrolDetailsView> viewProvider,
                        final Provider<MainMapView> mapViewProvider,
                        final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.mapViewProvider = mapViewProvider;
        this.props = props;
    }

    /**
     * Show the airfield details dialog.
     *
     * @param patrols The patrols for the selected radius.
     */
    public void show(final List<PatrolGroup> patrols) {
        airbaseGroup = patrols.get(0).getHomeGroup();

        dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        view = viewProvider.get();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Patrol Details");

        dialog.setWidth(props.getInt("patrol.dialog.width"));
        dialog.setHeight(props.getInt("patrol.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.show(patrols));

        patrols
                .stream()
                .map(PatrolGroup::getType)
                .forEach(patrolType ->
                        view
                                .getLabelMap()
                                .get(patrolType)
                                .forEach(this::registerHandlers));

        dialog.getOkButton().setOnAction(event -> ok());

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register the label event handlers.
     *
     * @param label The patrol radius label.
     */
    private void registerHandlers(final Label label) {
        label.setOnMouseClicked(this::radiusClicked);
        label.setOnMouseEntered(this::enterLabel);
        label.setOnMouseExited(this::exitLabel);
    }

    /**
     * Close this dialog.
     */
    private void ok() {
        mapViewProvider.get().unhighlightPatrolRadius(airbaseGroup);
        stage.close();
    }

    /**
     * Callback for when the mouse enter's the radius label.
     * Changes the cursor to indicate the label may be clicked.
     *
     * @param event The mouse event.
     */
    private void enterLabel(final MouseEvent event) {
        Label label = (Label) event.getSource();
        InnerShadow innerShadow = new InnerShadow(SHADOW_RADIUS, Color.BLACK);
        label.setEffect(innerShadow);
        dialog.getScene().setCursor(Cursor.HAND);
    }

    /**
     * Callback for when the mouse exits the radius label.
     * Changes the cursor to the default.
     *
     * @param event The mouse event.
     */
    private void exitLabel(final MouseEvent event) {
        Label label = (Label) event.getSource();
        label.setEffect(null);
        dialog.getScene().setCursor(Cursor.DEFAULT);
    }

    /**
     * Callback for when a patrol radii is clicked.
     *
     * @param event The mouse click event.
     */
    private void radiusClicked(final MouseEvent event) {
        Label label = (Label) event.getSource();

        int radius = (int) label.getUserData();

        unhighlightLabel();
        mapViewProvider.get().unhighlightPatrolRadius(airbaseGroup);

        if (label != highlighted) {
            highlightLabel(label);
            mapViewProvider.get().highlightPatrolRadius(airbaseGroup, radius);
        }
    }

    /**
     * Highlight the selected radius label.
     *
     * @param label The selected radius label.
     */
    private void highlightLabel(final Label label) {
        highlighted = label;

        label.setStyle("-fx-background-color: grey;"
                     + "-fx-text-fill: white;");
    }

    /**
     * Unhighlight the selected radius label.
     */
    private void unhighlightLabel() {
        Optional.ofNullable(highlighted).ifPresent(label ->
            label.setStyle("-fx-background-color: whitesmoke;"
                         + "-fx-text-fill: black;"));
    }
}

package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SquadronSummaryView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private final Label title = new Label();

    private final VBox mainPane =  new VBox();
    private final ImageView aircraftProfile = new ImageView();


    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     */
    @Inject
    public SquadronSummaryView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
    }

    /**
     * Show the squadron summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     *
     * @return A node containing the squadron summary.
     */
    public Node show(final Nation nation) {
        title.setId("summary-title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("summary-title-pane-" + nation.getFileName().toLowerCase());

        Node profile = buildProfile();

        HBox hBox = new HBox(profile);

        VBox vBox = new VBox(titlePane, hBox);

        mainPane.getChildren().add(vBox);
        mainPane.setVisible(false);
        mainPane.setId("summary-pane");

        return mainPane;
    }

    /**
     * Set the squadron for which the summary is shown.
     *
     * @param squadron The squadron who's summary is shown.
     */
    public void setSquadron(final Squadron squadron) {
        title.setText(squadron.getTitle());
        aircraftProfile.setImage(getProfile(squadron));
        mainPane.setVisible(true);
    }

    /**
     * Build the aircraft profile image.
     *
     * @return The node containing the aircraft profile image.
     */
    private Node buildProfile() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Profile");

        VBox vBox = new VBox(aircraftProfile);
        vBox.setId("profile-vbox");

        titledPane.setContent(vBox);
        titledPane.setMinHeight(props.getInt("ship.dialog.profile.height"));
        titledPane.setMaxHeight(props.getInt("ship.dialog.profile.height"));
        titledPane.setMinWidth(props.getInt("airfield.dialog.ready.width"));
        titledPane.setMaxWidth(props.getInt("airfield.dialog.ready.width"));
        return titledPane;
    }

    /**
     * Build the squadron stats.
     *
     * @return A node with the squadron stats.
     */
    private Node buildStats() {
        TitledPane titledPane = new TitledPane();


        return titledPane;
    }


    /**
     * Get the aircraft's profile image.
     *
     * @param squadron The selected squadron.
     * @return The aircraft's profile image view.
     */
    private Image getProfile(final Squadron squadron) {
        Aircraft aircraft = squadron.getAircraft();
        return imageResourceProvider.getAircraftProfileImageView(squadron.getSide(), aircraft.getModel() + "-240");
    }
}

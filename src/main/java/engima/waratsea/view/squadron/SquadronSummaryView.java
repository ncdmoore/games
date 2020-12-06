package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SquadronSummaryView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private final Label title = new Label();
    private final VBox mainPane =  new VBox();
    private final ImageView aircraftProfile = new ImageView();

    private final BoundTitledGridPane attackPane = new BoundTitledGridPane();
    private final BoundTitledGridPane performancePane = new BoundTitledGridPane();

    @Inject
    public SquadronSummaryView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
    }

    /**
     * Show the selectedSquadron summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     *
     * @return A node containing the selectedSquadron summary.
     */
    public Node build(final Nation nation) {
        Node titleNode = buildTitle(nation);
        Node profilePane = buildProfile();

        buildAttack();
        buildPerformance();

        HBox hBox = new HBox(profilePane, attackPane, performancePane);
        hBox.setId("summary-hbox");

        VBox vBox = new VBox(titleNode, hBox);
        vBox.setId("summary-vbox");

        mainPane.getChildren().add(vBox);
        mainPane.setVisible(false);
        mainPane.setId("summary-pane");

        return mainPane;
    }

    /**
     * Bind this view to a squadron view model.
     *
     * @param viewModel The squadron view model.
     */
    public void bind(final SquadronViewModel viewModel) {
        title.textProperty().bind(viewModel.getTitle());
        aircraftProfile.imageProperty().bind(viewModel.getAircraftProfileSummary());
        mainPane.visibleProperty().bind(viewModel.getPresent());

        attackPane.bindListStrings(viewModel.getAttackSummary());
        performancePane.bindStrings(viewModel.getPerformanceSummary());
    }

    private Node buildTitle(final Nation nation) {
        title.setId("summary-title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("summary-title-pane-" + nation.getFileName().toLowerCase());

        return titlePane;
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
        titledPane.setMinHeight(props.getInt("airfield.dialog.profile.height"));
        titledPane.setMaxHeight(props.getInt("airfield.dialog.profile.height"));
        titledPane.setMinWidth(props.getInt("airfield.dialog.profile.width"));
        titledPane.setMaxWidth(props.getInt("airfield.dialog.profile.width"));
        return titledPane;
    }

    private void buildAttack() {
        ImageView imageView = imageResourceProvider.getImageView(props.getString("info.small.image"));
        HBox hbox = new HBox(imageView);
        hbox.setId("squadron-attack-data-title");

        Tooltip tooltip = new Tooltip("Squadron Attack Data:\n\nFirst number is the attack factor.\nSecond number is the attack (modifier).\nD indicates Defensive Only.\nThird number is the % chance of a single hit.");

        tooltip.setShowDelay(Duration.seconds(props.getDouble("tooltip.delay")));
        tooltip.setShowDuration(Duration.seconds(props.getDouble("tooltip.duration")));

        Tooltip.install(imageView, tooltip);

        buildPane(attackPane);

        attackPane.setTitle("Squadron Attack Data");
        attackPane.setContentDisplay(ContentDisplay.RIGHT);
        attackPane.setGraphic(hbox);
    }


    private void buildPerformance() {
        ImageView imageView = imageResourceProvider.getImageView(props.getString("info.small.image"));
        HBox hbox = new HBox(imageView);
        hbox.setId("squadron-performance-data-title");

        Tooltip tooltip = new Tooltip("Squadron Performance Data:\n\nRadius is in grid squares.\n"
                + "Endurance is in game turns.\nThe mission type may affect endurance.\n"
                + "If drop tanks are needed,\nthey are automatically equipped.");

        tooltip.setShowDelay(Duration.seconds(props.getDouble("tooltip.delay")));
        tooltip.setShowDuration(Duration.seconds(props.getDouble("tooltip.duration")));

        Tooltip.install(imageView, tooltip);

        buildPane(performancePane);
        performancePane.setTitle("Squadron Performance Data");
        performancePane.setContentDisplay(ContentDisplay.RIGHT);
        performancePane.setGraphic(hbox);
    }

    /**
     * Build a component pane.
     *
     * @param pane The pane to build.
     */
    private void buildPane(final BoundTitledGridPane pane) {
         pane.setWidth(props.getInt("airfield.dialog.profile.width"))
                .setGridStyleId("component-grid")
                .build();
    }
}

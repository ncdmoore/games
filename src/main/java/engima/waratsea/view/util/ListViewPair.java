package engima.waratsea.view.util;

import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

public class ListViewPair<T> {
    private final String name;

    @Setter private int width;
    @Setter private int height;
    @Setter private int buttonWidth;
    @Getter private final ListView<T> available = new ListView<>();
    @Getter private final ListView<T> assigned = new ListView<>();
    @Getter private final Button add = new Button();
    @Getter private final Button remove = new Button();

    private final Label availableLabel = new Label();
    private final Label assignedLabel = new Label();

    private final ViewProps props;
    private final ResourceProvider resourceProvider;

    /**
     * Constructor called by guice.
     *  @param name The name used in CSS.
     * @param props The view properties.
     * @param resourceProvider Provides images.
     */
    public ListViewPair(final String name, final ViewProps props, final ResourceProvider resourceProvider) {
        this.name = name;
        this.props = props;
        this.resourceProvider = resourceProvider;
    }

    /**
     * Set the available label.
     *
     * @param title The text contained within the label.
     */
    public void setAvailableTitle(final String title) {
        availableLabel.setText(title);
    }

    /**
     * Set the assigned label.
     *
     * @param title The text contained within the label.
     */
    public void setAssignedTitle(final String title) {
        assignedLabel.setText(title);
    }

    /**
     * Build the listView pair.
     *
     * @return A node that contains the listView pair.
     */
    public Node build() {
        add.setGraphic(resourceProvider.getImageView(props.getString("right.arrow.image")));
        remove.setGraphic(resourceProvider.getImageView(props.getString("left.arrow.image")));

        available.setMinWidth(width);
        available.setMaxWidth(width);
        available.setMinHeight(height);
        available.setMaxHeight(height);

        assigned.setMinWidth(width);
        assigned.setMaxWidth(width);
        assigned.setMinHeight(height);
        assigned.setMaxHeight(height);

        add.setMinWidth(buttonWidth);
        add.setMaxWidth(buttonWidth);
        remove.setMinWidth(buttonWidth);
        remove.setMaxWidth(buttonWidth);

        VBox buttonVBox = new VBox(add, remove);
        buttonVBox.setId(name + "-list-view-pair-controls");

        VBox availableVBox = new VBox(availableLabel, available);
        VBox assignedVBox = new VBox(assignedLabel, assigned);

        HBox hBox = new HBox(availableVBox, buttonVBox, assignedVBox);
        hBox.setId(name + "-list-view-pair");

        return hBox;
    }

    /**
     * Add a element to the assigned list from the available list.
     *
     * @param t the element added to the assigned list.
     */
    public void add(final T t) {
        Optional.ofNullable(t).ifPresent(element -> {
            available.getItems().remove(t);
            assigned.getItems().add(t);
            remove.setDisable(false);
        });
    }

    /**
     * Add an element to the available list from the assigned list.
     *
     * @param t the element added to the available list.
     */
    public void remove(final T t) {
        Optional.ofNullable(t).ifPresent(element -> {
            assigned.getItems().remove(t);
            available.getItems().add(t);
        });
    }

    /**
     * Clear both lists.
     */
    public void clearAll() {
        available.getItems().clear();
        assigned.getItems().clear();
    }
}

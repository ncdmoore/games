package engima.waratsea.view.util;

import engima.waratsea.utility.ImageResourceProvider;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

public class ListViewPair<T> {

    private final String name;

    @Setter
    private int width;

    @Setter
    private int height;

    @Setter
    private int buttonWidth;

    private Label availableLabel = new Label();

    @Getter
    private ListView<T> available = new ListView<>();

    private Label assignedLabel = new Label();

    @Getter
    private ListView<T> assigned = new ListView<>();

    @Getter
    private Button add = new Button();

    @Getter
    private Button remove = new Button();

    private final ImageResourceProvider imageResourceProvider;

    /**
     * Constructor called by guice.
     *
     * @param name The name used in CSS.
     * @param imageResourceProvider Provides images.
     */
    public ListViewPair(final String name, final ImageResourceProvider imageResourceProvider) {
        this.name = name;
        this.imageResourceProvider = imageResourceProvider;
        add.setDisable(true);
        remove.setDisable(true);
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
        add.setGraphic(imageResourceProvider.getImageView("rightArrow.png"));
        remove.setGraphic(imageResourceProvider.getImageView("leftArrow.png"));

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

            if (available.getItems().isEmpty()) {
                add.setDisable(true);
            }

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
            add.setDisable(false);

            if (assigned.getItems().isEmpty()) {
                remove.setDisable(true);
            }
        });
    }

    /**
     * Remove an element from the available list.
     *
     * @param t the element that is removed from the available list.
     */
    public void removeFromAvailable(final T t) {
        Optional
                .ofNullable(t)
                .ifPresent(element -> {
                    available.getItems().remove(element);

                    if (available.getItems().isEmpty()) {
                        add.setDisable(true);
                    }
                });
    }

    /**
     * Add an element to the available list.
     *
     * @param t the element that is added to the available list.
     */
    public void addToAvailable(final T t) {
        Optional
                .ofNullable(t)
                .ifPresent(element -> {
                    available.getItems().add(element);
                    add.setDisable(false);
                });
    }

    /**
     * Add the given list to the available list.
     *
     * @param t The list to add.
     */
    public void addAllToAvailable(final List<T> t) {
        available.getItems().addAll(t);
        if (!t.isEmpty()) {
            add.setDisable(false);
        }
    }

    /**
     * Add the given list to the assigned list.
     *
     * @param t The list to add.
     */
    public void addAllToAssigned(final List<T> t) {
        assigned.getItems().addAll(t);
        if (!t.isEmpty()) {
            remove.setDisable(false);
        }
    }

    /**
     * Clear the current available selection.
     **/
    public void clearAvailableSelection() {
        // Javafx does not call this callback if the element does not change.
        // This causes problems when we click between the available list
        // and the assigned list. It's possible for the value to not change.
        // Thus, when the assigned is selected we clear the selection
        // in the available list. That way when we click back into the available list
        // the value is guaranteed to change.
        available.getSelectionModel().clearSelection();
    }

    /**
     * Clear the current assigned selection.
     **/
    public void clearAssignedSelection() {
        // Javafx does not call this callback if the element does not change.
        // This causes problems when we click between the available list
        // and the assigned list. It's possible for the value to not change.
        // Thus, when the assigned is selected we clear the selection
        // in the available list. That way when we click back into the available list
        // the value is guaranteed to change.
        assigned.getSelectionModel().clearSelection();
    }

    /**
     * Clear both the current available selection and the current assigned selection.
     */
    public void clearSelections() {
        clearAvailableSelection();
        clearAssignedSelection();
    }


    /**
     * Clear both lists.
     */
    public void clearAll() {
        available.getItems().clear();
        assigned.getItems().clear();
        add.setDisable(true);
        remove.setDisable(true);
    }
}

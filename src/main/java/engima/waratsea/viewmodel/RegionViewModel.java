package engima.waratsea.viewmodel;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.map.region.Region;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

public class RegionViewModel {

    @Getter private ObjectProperty<ObservableList<Airfield>> airfields = new SimpleObjectProperty<>(this, "airfields", FXCollections.emptyObservableList());

    @Getter private StringProperty minSteps = new SimpleStringProperty();
    @Getter private StringProperty maxSteps = new SimpleStringProperty();
    @Getter private StringProperty currentSteps = new SimpleStringProperty();
    @Getter private BooleanProperty minimumSatisfied = new SimpleBooleanProperty();


    private Region region;
    /**
     * Set the model.
     *
     * @param selectedRegion The selected region.
     */
    public void setModel(final Region selectedRegion) {
        region = selectedRegion;

        if (region == null) {
            return;
        }

        minSteps.set(region.getMinSteps() + "");
        maxSteps.set(region.getMaxSteps() + "");
        currentSteps.set(region.getCurrentSteps() + "");
        minimumSatisfied.set(region.hasMinimumRequirement());
        airfields.set(FXCollections.observableArrayList(region.getAirfields()));
    }

    /**
     * Refresh the region view model from the model.
     */
    public void refresh() {
        currentSteps.set(region.getCurrentSteps() + "");
        minimumSatisfied.set(region.hasMinimumRequirement());
    }
}

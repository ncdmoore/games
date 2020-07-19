package engima.waratsea.view.map.marker.main;

import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.MarkerGrid;
import engima.waratsea.model.target.Target;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Draws the mission markers for each mission from the originating airbase to the target.
 */
@Slf4j
public class MissionMarkers {
    @Getter private List<MissionMarker> missionMarkers = Collections.emptyList();
    @Setter private EventHandler<? super MouseEvent> arrowMouseHandler;

    private final MapView mapView;
    private final MarkerGrid originationGrid;    //Base or task force grid.
    private final GridView originationGridView;  //Base or task force grid view.

    /**
     * Constructor.
     *
     * @param mapView The map view.
     * @param originationGrid The base or task force grid of the mission origination.
     * @param originationGridView The grid view of the base or task force grid.
     */
    public MissionMarkers(final MapView mapView, final MarkerGrid originationGrid, final GridView originationGridView) {
        this.mapView = mapView;
        this.originationGrid = originationGrid;
        this.originationGridView = originationGridView;
    }

    /**
     * Draw all the mission markers.
     */
    public void draw() {
        List<MissionMarker> newMarkers = originationGrid
                .getMissions()
                .map(missions -> missions
                        .entrySet()
                        .stream()
                        .map(this::drawMarker)
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);

        // Get any missions that are no longer needed.
        List<MissionMarker> removed = ListUtils.subtract(missionMarkers, newMarkers);

        // Remove the unneeded missions.
        removed.forEach(MissionMarker::remove);

        missionMarkers = newMarkers;
    }

    /**
     * Hide all the mission markers.
     */
    public void hide() {
        missionMarkers.forEach(MissionMarker::remove);
    }

    /**
     * Draw an individual mission marker.
     *
     * @param entry A map entry with the mission target as key and a list of missions as the value.
     *              All of the missions have the target key as their target.
     * @return A mission marker.
     */
    private MissionMarker drawMarker(final Map.Entry<Target, List<AirMission>> entry) {
        GameGrid targetGrid = entry
                .getKey()
                .getGrid()
                .orElseThrow();

        GridView targetGridView = new GridView(originationGridView.getSize(), targetGrid);

        MissionMarker missionMarker = new MissionMarker(mapView, originationGridView, targetGridView);

        missionMarker.draw(entry.getValue());
        missionMarker.add();
        missionMarker.setClickHandler(arrowMouseHandler);

        return missionMarker;
    }
}

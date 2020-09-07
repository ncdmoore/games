package engima.waratsea.view.map.marker.main;

import com.google.inject.name.Named;
import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.model.map.TaskForceGrid;
import engima.waratsea.model.map.region.RegionGrid;
import engima.waratsea.view.map.MapView;

/**
 * Factory used by guice to create base markers.
 */
public interface BaseMarkerFactory {
    /**
     * Creates a base marker.
     *
     * @param baseGrid A base grid.
     * @param mapView A map view.
     * @return A base marker.
     */
    @Named("base")
    BaseMarker createBaseMarker(BaseGrid baseGrid, MapView mapView);

    /**
     * Creates a task force marker.
     *
     * @param taskForceGrid A task force grid.
     * @param mapView A map view.
     * @return A task force marker.
     */
    @Named("taskforce")
    TaskForceMarker createTaskForceMarker(TaskForceGrid taskForceGrid, MapView mapView);

    /**
     * Creates a region marker.
     *
     * @param regionGrid A game map region grid.
     * @param mapView A map view.
     * @return A region marker.
     */
    @Named("region")
    RegionMarker createRegionMarker(RegionGrid regionGrid, MapView mapView);
}

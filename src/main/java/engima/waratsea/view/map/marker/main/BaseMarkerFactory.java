package engima.waratsea.view.map.marker.main;

import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.view.map.GridView;

/**
 * Factory used by guice to create base markers.
 */
public interface BaseMarkerFactory {
    /**
     * Creates a base marker.
     *
     * @param baseGrid A base grid.
     * @param gridView A grid view.
     * @return A base marker.
     */
    BaseMarker create(BaseGrid baseGrid,
                      GridView gridView);
}

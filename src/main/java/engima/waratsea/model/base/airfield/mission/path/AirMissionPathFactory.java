package engima.waratsea.model.base.airfield.mission.path;

import com.google.inject.name.Named;
import engima.waratsea.model.base.airfield.mission.path.data.AirMissionPathData;

/**
 * Creates air mission path objects.
 */
public interface AirMissionPathFactory {
    /**
     * Create an air mission path.
     *
     * @param data The air mission path data.
     * @return A round trip air mission path initialized with the given data.
     */
    @Named("roundTrip")
    AirMissionPath createRoundTrip(AirMissionPathData data);

    /**
     * Create an airbase land strike mission.
     *
     * @param data The mission data.
     * @return A land strike mission initialized with the given data.
     */
    @Named("oneWay")
    AirMissionPath createOneWay(AirMissionPathData data);
}

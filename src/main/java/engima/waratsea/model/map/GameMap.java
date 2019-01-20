package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionLoader;
import engima.waratsea.model.scenario.Scenario;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the game map. This is essentially the game board. It is not a GUI component.
 *
 */
@Slf4j
@Singleton
public final class GameMap {

    private static final String MAP_REFERENCE_FORMAT = "\\s*([a-zA-Z]{1,2})(\\d{1,2})\\s*";

    private MapProps props;
    private RegionLoader regionLoader;

    private Map<Side, List<Region>> regions = new HashMap<>();
    private Map<Side, List<String>> airfields = new HashMap<>();
    private Map<Side, List<String>> ports = new HashMap<>();
    private Map<Side, Map<String, String>> locationToBaseMap = new HashMap<>();

    /**
     * The constructor of the GameMap. Called by guice.
     * @param props Map properties.
     * @param regionLoader loads the region data.
     */
    @Inject
    public GameMap(final MapProps props,
                   final RegionLoader regionLoader) {
        this.props = props;
        this.regionLoader = regionLoader;
    }

    /**
     * Load the map.
     * @param scenario The selected scenario.
     * @throws MapException An error occured attempting to load the map data.
     */
    public void load(final Scenario scenario) throws MapException {
        regions.put(Side.ALLIES, regionLoader.loadRegions(scenario, Side.ALLIES));
        regions.put(Side.AXIS, regionLoader.loadRegions(scenario, Side.AXIS));
        buildLocationToBaseMap();
    }

    /**
     * Get a list of a given sides airfields.
     * @param side The side ALLIES or AXIS.
     * @return A list of airfield names.
     */
    public List<String> getAirfields(final Side side) {
        return Optional.ofNullable(airfields.get(side))
                .orElseGet(() -> parseAirfieldNames(side));
    }

    /**
     * Get a list of a given sides ports.
     * @param side The side ALLIES or AXIS.
     * @return A list of port names.
     */
    public List<String> getPorts(final Side side) {
        return Optional.ofNullable(ports.get(side))
                .orElseGet(() -> parsePortNames(side));
    }


    /**
     * Determine if the given location is a base for the given side.
     * @param side The side ALLIES or AXIS.
     * @param location The map reference location.
     * @return True if the given location is a base for the given side.
     */
    public boolean isLocationBase(final Side side, final String location) {
        return locationToBaseMap.get(side).containsKey(convertNameToReference(location));
    }

    /**
     * Convert a location name to a map reference. For example, the name Gibraltar is converted to G23.
     * @param name A named location on the map.
     * @return The corresponding map reference of where the name is located.
     */
    public String convertNameToReference(final String name) {
        Pattern pattern = Pattern.compile(MAP_REFERENCE_FORMAT);
        Matcher matcher = pattern.matcher(name);
        return  matcher.matches() ? name : props.getString(name);
    }

    /**
     * Convert a game map reference into grid coordinates.
     * @param mapReference game map reference.
     * @return a map grid coordinate.
     */
    public Grid getGrid(final String mapReference) {

        Pattern pattern = Pattern.compile(MAP_REFERENCE_FORMAT);
        Matcher matcher = pattern.matcher(mapReference);

        int row = 0;
        int column = 0;

        while (matcher.find()) {
            String columnLabel = matcher.group(1);
            String rowLabel = matcher.group(2);
            row = Integer.parseInt(rowLabel) - 1;      // zero-based row numbers.
            column = convertColumn(columnLabel);       // zero-based column numbers.
        }


        log.info("Map reference: {}", mapReference);
        log.info("Grid row: {}, column: {}", row, column);

        return new Grid(row, column);
    }

    /**
     * Convert an array of numeric base 26 characters to an integer. For example, the column label AA is converted into
     * 26. The first A = 26, second A = 1. Thus, 26 + 1 = 27. But, since the columns are zero based AA is equal 26.
     *
     * @param columnLabel String that represents the column on the game map.
     * @return The integer equivalent of the given column string.
     */
    private int convertColumn(final String columnLabel) {

        char[] numericCharacters = columnLabel.toCharArray();
        final int alphabetSize = 26;

        int value = 0;

        for (char c : numericCharacters) {
            char base = 'a';
            if (Character.isUpperCase(c)) {
                base = 'A';
            }
            value = (value * alphabetSize) + ((c - base) + 1);
        }

        return value - 1;
    }

    /**
     * Build a location map reference to base name map. This allows the game map to quickly determine if a
     * given location is corresponds to a base.
     */
    private void buildLocationToBaseMap() {
        buildLocationToBaseMap(Side.ALLIES);
        buildLocationToBaseMap(Side.AXIS);
    }

    /**
     * Build a location map reference to base name map for the given side.
     * @param side The side ALLIES or AXIS.
     */
    private void buildLocationToBaseMap(final Side side) {
         Map<String, String> baseMap = Stream.of(getAirfields(side),
                getPorts(side))
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toMap(baseName -> props.getString(baseName), // map reference is the key
                                          baseName -> baseName,                  // base name is the value.
                                          (oldValue, newValue) -> newValue));    // For duplicate keys use the new value.

         locationToBaseMap.put(side, baseMap);
    }

    /**
     * Get a list of airfield names for the given side from the region data.
     * @param side The airfields' side ALLIES or AXIS.
     * @return A list of airfield names.
     */
    private List<String> parseAirfieldNames(final Side side) {
        List<String> airfieldNames =  regions.get(side)
                .stream()
                .flatMap(region -> region.getAirfields().stream())
                .collect(Collectors.toList());

        airfields.put(side, airfieldNames);
        return airfieldNames;
    }

    /**
     * Get a list of port names for the given side from the region data.
     * @param side The port's side ALLIES or AXIS.
     * @return A list of port names.
     */
    public List<String> parsePortNames(final Side side) {
        List<String> portNames = regions.get(side)
                .stream()
                .flatMap(region -> region.getPorts().stream())
                .collect(Collectors.toList());

        ports.put(side, portNames);
        return portNames;
    }
}

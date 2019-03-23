package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldLoader;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortLoader;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionLoader;
import engima.waratsea.model.scenario.Scenario;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
    private static final Pattern PATTERN = Pattern.compile(MAP_REFERENCE_FORMAT);
    private static final String ANY_ENEMY_BASE = "ANY_ENEMY_BASE";
    private static final String ANY_FRIENDLY_BASE = "ANY_FRIENDLY_BASE";

    private RegionLoader regionLoader;

    @Getter
    private final int rows;

    @Getter
    private final int columns;

    private final AirfieldLoader airfieldLoader;
    private final PortLoader portLoader;

    private Map<Side, List<Region>> regions = new HashMap<>();
    private Map<Side, List<Airfield>> airfields = new HashMap<>();
    private Map<Side, List<Port>> ports = new HashMap<>();
    private Map<Side, Map<String, String>> baseRefToName = new HashMap<>();
    private Map<Side, Map<String, String>> baseNameToRef = new HashMap<>();

    /**
     * The constructor of the GameMap. Called by guice.
     *
     * @param props Map properties.
     * @param regionLoader loads the region data.
     * @param airfieldLoader loads the airfields.
     * @param portLoader laods the ports.
     */
    @Inject
    public GameMap(final MapProps props,
                   final RegionLoader regionLoader,
                   final AirfieldLoader airfieldLoader,
                   final PortLoader portLoader) {
        this.regionLoader = regionLoader;
        this.airfieldLoader = airfieldLoader;
        this.portLoader = portLoader;

        rows = props.getInt("rows");
        columns = props.getInt("columns");
    }

    /**
     * Load the map.
     *
     * @param scenario The selected scenario.
     * @throws MapException An error occured attempting to load the map data.
     */
    public void load(final Scenario scenario) throws MapException {
        regions.put(Side.ALLIES, regionLoader.loadRegions(scenario, Side.ALLIES));
        regions.put(Side.AXIS, regionLoader.loadRegions(scenario, Side.AXIS));

        airfields.put(Side.ALLIES, buildAirfields(Side.ALLIES));
        airfields.put(Side.AXIS, buildAirfields(Side.AXIS));

        ports.put(Side.ALLIES, buildPorts(Side.ALLIES));
        ports.put(Side.AXIS, buildPorts(Side.AXIS));

        buildLocationToBaseMap();
    }

    /**
     * Get a side's airfields.
     *
     * @param side The side ALLIES or AXIS.
     * @return The side's airfields.
     */
    public List<Airfield> getAirfields(final Side side) {
        return airfields.get(side);
    }

    /**
     * Get a side's ports.
     *
     * @param side The side ALLIES or AXIS.
     * @return The side's ports.
     */
    public List<Port> gerPorts(final Side side) {
        return ports.get(side);
    }


    /**
     * Get all of the bases on this map.
     *
     * @return A list of all the game bases.
     */
    public List<Base> getBases() {
        return Stream.concat(getBases(Side.ALLIES), getBases(Side.AXIS))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Determine if the given location is a base for the given side.
     *
     * @param side The side ALLIES or AXIS.
     * @param location The map reference location.
     * @return True if the given location is a base for the given side.
     */
    public boolean isLocationBase(final Side side, final String location) {
        return baseRefToName.get(side).containsKey(convertNameToReference(location));
    }

    /**
     * Convert a location name to a map reference. For example, the name Gibraltar is converted to H22.
     *
     * @param name A named location on the map.
     * @return The corresponding map reference of where the name is located.
     */
    public String convertNameToReference(final String name) {
        if (name == null) {
            log.error("Location is null");
            return null;
        }

        Matcher matcher = PATTERN.matcher(name);
        return   matcher.matches() ? name : Optional.ofNullable(getBaseReference(name)).orElse(name);
    }

    /**
     * Convert a map reference to a location name. For example, the reference H22 is converted to Gibraltar.
     *
     * @param reference A map reference.
     * @return The corresponding location name.
     */
    public String convertReferenceToName(final String reference) {
        return Optional.ofNullable(getBaseName(reference)).orElse(reference);
    }

    /**
     * Convert a game map reference into grid coordinates.
     *
     * @param mapReference game map reference.
     * @return a map grid coordinate.
     */
    public GameGrid getGrid(final String mapReference) {

        if (mapReference == null) {
            log.error("Cannot get grid. Map reference is null");
            return null;
        }

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


        log.debug("Map reference: {}", mapReference);
        log.debug("Grid row: {}, column: {}", row, column);

        return new GameGrid(row, column);
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
     * given location corresponds to a base.
     */
    private void buildLocationToBaseMap() {
        buildLocationToBaseMap(Side.ALLIES);
        buildLocationToBaseMap(Side.AXIS);
    }

    /**
     * Build a location map reference to base name map for the given side. Both ports and airfields are
     * included in this map.
     *
     * @param side The side ALLIES or AXIS.
     */
    private void buildLocationToBaseMap(final Side side) {
        Map<String, String> refToNameMap = getBases(side)
                .distinct()
                .collect(Collectors.toMap(Base::getReference,
                                          Base::getName,
                                          (oldValue, newValue) -> newValue));

        Map<String, String> nameToRefMap = getBases(side)
                .distinct()
                .collect(Collectors.toMap(Base::getName,
                                          Base::getReference,
                                          (oldValue, newValue) -> newValue));

        baseRefToName.put(side, refToNameMap);
        baseNameToRef.put(side, nameToRefMap);
    }

    /**
     * Get a stream of all the bases both airfields and ports.
     *
     * @param side The side ALLIES or AXIS.
     * @return A stream of bases.
     */
    private Stream<Base> getBases(final Side side) {
        return Stream.concat(airfields.get(side).stream(), ports.get(side).stream());
    }

    /**
     * Get the base map reference given the base name.
     *
     * @param name The name of the base.
     * @return The map reference of the base.
     */
    private String getBaseReference(final String name) {
        String ref = baseNameToRef.get(Side.ALLIES).get(name);
        if (ref == null) {
            ref = baseNameToRef.get(Side.AXIS).get(name);
        }

        return ref;
    }

    /**
     * Get the base name given the base reference.
     *
     * @param reference The base's map reference.
     * @return The name of the base.
     */
    private String getBaseName(final String reference) {
        String name = baseRefToName.get(Side.ALLIES).get(reference);
        if (name == null) {
            name = baseRefToName.get(Side.AXIS).get(reference);
        }

        return name;
    }

    /**
     * Get a list of airfield names for the given side from the region data.
     *
     * @param side The airfields' side ALLIES or AXIS.
     * @return A list of airfield names.
     */
    private List<Airfield> buildAirfields(final Side side) {
        List<String> airfieldNames =  regions.get(side)
                .stream()
                .flatMap(region -> region.getAirfields().stream())
                .collect(Collectors.toList());

        return airfieldLoader.load(side, airfieldNames);
    }

    /**
     * Get a list of port names for the given side from the region data.
     *
     * @param side The port's side ALLIES or AXIS.
     * @return A list of port names.
     */
    private List<Port> buildPorts(final Side side) {
        List<String> portNames = regions.get(side)
                .stream()
                .flatMap(region -> region.getPorts().stream())
                .collect(Collectors.toList());

        return portLoader.load(side, portNames);
    }
}

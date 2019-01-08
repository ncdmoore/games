package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the game map. This is essentially the game board. It is not a GUI component.
 *
 */
@Slf4j
@Singleton
public final class GameMap {

    private static final String MAP_REFERENCE_FORMAT = "\\s*([a-zA-Z]{1,2})(\\d{1,2})\\s*";

    private MapProps props;

    /**
     * The constructor of the GameMap. Called by guice.
     * @param props Map properties.
     */
    @Inject
    public GameMap(final MapProps props) {
        this.props = props;
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


}

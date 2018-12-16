package engima.waratsea.model.map;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the game map.
 *
 */
@Slf4j
public final class GameMap {

    /**
     * Convert a game map reference into grid coordinates.
     * @param mapRefernce game map reference.
     * @return a map grid coordinate.
     */
    public Grid getGrid(final String mapRefernce) {

        Pattern pattern = Pattern.compile("\\s*([a-zA-Z]+)(\\d+)\\s*");
        Matcher matcher = pattern.matcher(mapRefernce);

        int row = 0;
        int column = 0;

        while (matcher.find()) {
            String columnLabel = matcher.group(1);
            String rowLabel = matcher.group(2);
            row = Integer.parseInt(rowLabel) - 1;      // zero-based row numbers.
            column = convertColumn(columnLabel);       // zero-based column numbers.
        }


        log.info("Map reference: {}", mapRefernce);
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

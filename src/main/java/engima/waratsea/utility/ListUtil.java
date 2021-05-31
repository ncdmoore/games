package engima.waratsea.utility;

import java.util.ArrayList;
import java.util.List;

/**
 * List utility class. It aids with stream operations.
 */
public final class ListUtil {

    private ListUtil() {
    }

    /**
     * Create a list of Patrols from a pair of radius, patrol.
     *
     * @param <T> The type that the list contains.
     * @param t  A radius, patrol pair.
     * @return A list of Patrols.
     */
    public static  <T> List<T> createList(final T t) {
        List<T> list = new ArrayList<>();
        list.add(t);
        return list;
    }
}

package engima.waratsea.utility;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
    /**
     * Create a list of Patrols from a pair of radius, patrol.
     *
     * @param t  A radius, patrol pair.
     * @return A list of Patrols.
     */
    public static  <T> List<T> createList(final T t) {
        List<T> list = new ArrayList<>();
        list.add(t);
        return list;
    }
}

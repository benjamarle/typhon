package net.zorgblub.typhon;

import java.util.List;

import jedi.option.Option;

import static jedi.option.Options.none;
import static jedi.option.Options.some;

/**
 * Created by alex on 11/7/14.
 */
public class CollectionUtil {

    public static <T> Option<T> listElement( List<T> list, int itemIndex ) {
        if ( itemIndex >= 0 && itemIndex < list.size() ) {
            return some( list.get(itemIndex ) );
        } else {
            return none();
        }
    }

}

package org.rikai;

/**
 * Created by Benjamin on 17/09/2015.
 */
public class HtmlEntryUtils {

    public static String wrapColor(int color, String string) {
        String colorStr = String.format("#%06X", (0xFFFFFF & color));
        return wrapColor(colorStr, string);
    }

    public static String wrapColor(String color, String string) {
        return "<font color=\"" + color + "\">" + string + "</font>";
    }
}

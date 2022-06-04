package de.borekking.banSystem.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class JavaUtils {

    @SafeVarargs
    public static <T> List<T> getAsList(T... arr) {
        return new ArrayList<>(Arrays.asList(arr));
    }

    public static String concat(List<? extends CharSequence> list) {
        return getTextWithDelimiter(list, "");
    }

    public static String getTextWithDelimiter(List<? extends CharSequence> list, CharSequence delimiter) {
        return String.join(delimiter, list);
    }

    public static String[] mergeArrays(String[]... arrays) {
        int length = 0;
        for (String[] arr : arrays) {
            length += arr.length;
        }

        int i = 0;
        String[] array = new String[length];

        for(String[] arr : arrays) {
            for (String s : arr) {
                array[i] = s;
                i++;
            }
        }

        return array;
    }

    // {"hey", "was", "up"} -> "hey was up".
    public static String getArrayAsString(Object[] array, int fromIn, int endOut) {
        StringJoiner joiner = new StringJoiner(" ");

        for (int i = fromIn; i < endOut; i++) {
            Object o = array[i];
            if (o == null) continue;
            joiner.add(o.toString());
        }

        return joiner.toString();
    }
}

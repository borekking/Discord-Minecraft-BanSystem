package de.borekking.banSystem.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

public class JavaUtils {

    @SafeVarargs
    public static <T> List<T> getAsList(T... arr) {
        return new ArrayList<>(Arrays.asList(arr));
    }

    public static String concat(Collection<? extends CharSequence> list) {
        return getTextWithDelimiter(list, "");
    }

    public static String getTextWithDelimiter(Collection<? extends CharSequence> list, CharSequence delimiter) {
        return String.join(delimiter, list);
    }

    @SafeVarargs
    public static <T> T[] mergeArrays(Function<Integer, T[]> arrayCreator, T[]... arrays) {
        int length = 0;
        for (T[] arr : arrays) {
            length += arr.length;
        }

        T[] array = arrayCreator.apply(length);

        int i = 0;
        for (T[] arr : arrays) {
            for (T t : arr) {
                array[i] = t;
                i++;
            }
        }

        return array;
    }

    public static String[] mergeArrays(String[]... arrays) {
        return mergeArrays(String[]::new, arrays);
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

    public static <K, V> Map<K, V> createMap(Object... objects) {
        if (objects.length % 2 != 0) throw new RuntimeException("Odd size of objects");

        Map<K, V> map = new HashMap<>();

        for (int i = 0; i < objects.length - 1; i += 2) {
            K key = (K) objects[i];
            V value = (V) objects[i+1];

            map.put(key, value);
        }

        return map;
    }
}

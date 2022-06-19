package de.borekking.banSystem.duration;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum TimeEnum {

    // Enum containing different units of time from seconds to years (permanent = negatived too).
    // Duration as long is meant is milliseconds.

    SECOND("s", 1000L),
    MINUTE("min", SECOND.millis * 60L),
    HOUR("h", MINUTE.millis * 60L),
    DAY("d", HOUR.millis * 24L),
    WEEK("w", DAY.millis * 7L),
    MONTH("m", DAY.millis * 30L),
    YEAR("y", DAY.millis * 365L),
    PERMANENT("p", -1L);

    private static List<TimeEnum> increasingValues;

    private final String shortName, name;
    private final long millis;

    TimeEnum(String shortName, long millis) {
        this.shortName = shortName;
        this.name = this.name().toLowerCase();
        this.millis = millis;
    }

    public static List<TimeEnum> getDecreasingValues() {
        if (increasingValues == null) {
            increasingValues = Arrays.stream(values())
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
        }
        return increasingValues;
    }

    public static <T> TimeEnum getByAttribute(T t, Function<TimeEnum, T> converter) {
        for (TimeEnum time : TimeEnum.values()) {
            T cu = converter.apply(time);
            if (cu == null) continue; // Prevent NPE

            if (cu.equals(t)) return time;
        }

        return null;
    }

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return name;
    }

    public long getMillis() {
        return millis;
    }
}

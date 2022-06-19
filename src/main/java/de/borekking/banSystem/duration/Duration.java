package de.borekking.banSystem.duration;

import java.util.StringJoiner;

public final class Duration {

    private Duration() {
    }

    public static class IllegalDurationException extends Exception {
        public IllegalDurationException() {
        }
    }

    public static long getValueOf(String str) throws IllegalDurationException {
        return getValueOfMultiple(str);
    }

    // One or more string of form <a><t> (see below) separated by strings.
    private static long getValueOfMultiple(String str) throws IllegalDurationException {
        String[] arr = str.split(" ");
        if (arr.length == 0) return 0L;

        long total = 0L;

        for (String cu : arr) {
            long currentMillis = getValueOfOne(cu);

            // Check for permanent
            if (currentMillis < 0) return -1L;

            total += getValueOfOne(cu);
        }

        return total;
    }

    // Receives string of form <a><t> where <a> is an integer, and <t> is a shortname from TimeEnum
    // e.g. 7d, 23min.
    // Returns long value a * t.millis.
    private static long getValueOfOne(String str) throws IllegalDurationException {
        // Find index of first letter -> for <t>
        int len = str.length(), index = str.length();

        for (int i = 0; i < len; i++) {
            if (Character.isLetter(str.charAt(i))) {
                index = i;
                break;
            }
        }

        String amount = str.substring(0, index), name = str.substring(index).toLowerCase();
        TimeEnum time = TimeEnum.getByAttribute(name, TimeEnum::getShortName);

        if (time == null) throw new IllegalDurationException();

        int times = 1;
        try {
            times = Integer.parseInt(amount);
        } catch (NumberFormatException ignored) {
        }

        return times * time.getMillis();
    }

    // Get message of form <a_1><t_1> <a_2><t_2> ... <a_n><t_n> from long value
    public static String getMessage(long millis) {
        if (millis < 0) return "p";

        long rest = millis; // Millis left
        StringJoiner joiner = new StringJoiner(" ");

        for (TimeEnum time : TimeEnum.getIncreasingValues()) {
            long currentMillis = time.getMillis();
            if (currentMillis < 0) continue; // Skip p

            int amount = 0; // Amount of time used
            while (rest > currentMillis) {
                // Subtract current millis from rest and increment amount
                rest = rest - currentMillis;
                amount++;
            }

            if (amount > 0) { // time is used more than ones
                joiner.add(amount + " " + time.getName() + "(s)");
            }
        }

        return joiner.toString();
    }
}

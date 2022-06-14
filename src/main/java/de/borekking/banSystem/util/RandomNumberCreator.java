package de.borekking.banSystem.util;

import java.util.Random;

public final class RandomNumberCreator {

    private static Random random;

    private RandomNumberCreator() {
    }

    public static long getRandomLong() {
        updateRandom();

        return random.nextLong();
    }

    public static long getRandomLong(long from) {
        long l;

        do {
            l = getRandomLong();
        } while (l < from);

        return l;
    }

    private static void updateRandom() {
        if (random == null) {
            random = new Random();
        }
    }

}

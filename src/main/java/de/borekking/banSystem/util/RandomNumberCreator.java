package de.borekking.banSystem.util;

import java.util.Random;

public final class RandomNumberCreator {

    private static Random random;

    public static long getRandomLong() {
        if (random == null) {
            random = new Random();
        }

        return random.nextLong();
    }

}

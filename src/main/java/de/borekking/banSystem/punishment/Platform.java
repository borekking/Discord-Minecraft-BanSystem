package de.borekking.banSystem.punishment;

import java.util.Arrays;

public enum Platform {

    DISCORD("d"),
    MINECRAFT("m");

    private final String identifier;

    Platform(String identifier) {
        this.identifier = identifier;
    }

    public static Platform getByIdentifier(String identifier) {
        return Arrays.stream(Platform.values()).filter(p -> p.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    public String getIdentifier() {
        return identifier;
    }
}

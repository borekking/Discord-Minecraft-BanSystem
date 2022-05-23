package de.borekking.banSystem.punishment;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public enum Platform {

    DISCORD("discordUser", "discordID", "d"),
    MINECRAFT("minecraftUser", "minecraftID", "m");

    private final String databaseTableName, columnName, identifier;

    Platform(String databaseTableName, String columnName, String identifier) {
        this.databaseTableName = databaseTableName;
        this.columnName = columnName;
        this.identifier = identifier;
    }

    public static Platform getByIdentifier(String identifier) {
        return Arrays.stream(Platform.values()).filter(p -> p.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    public static List<UUID> convertMinecraftIDs(List<String> list) {
        return list.stream().map(UUID::fromString).collect(Collectors.toList());
    }

    public static List<Long> convertDiscordIDs(List<String> list) {
        return list.stream().map(Long::parseLong).collect(Collectors.toList());
    }

    public String getDatabaseTableName() {
        return databaseTableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getIdentifier() {
        return identifier;
    }
}

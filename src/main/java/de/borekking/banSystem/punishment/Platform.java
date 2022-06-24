package de.borekking.banSystem.punishment;

import de.borekking.banSystem.util.BSUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum Platform {

    DISCORD("discordUser", "discordID", "d",
            id -> { long discordID; return (discordID = BSUtils.getDiscordID(id)) >= 0 ? String.valueOf(discordID) : null; },
            id -> BSUtils.getDiscordID(id) >= 0,
            BSUtils::getUserIDByDiscord,
            BSUtils::getUserIDByDiscordIDAndCreateIfAbsent),
    MINECRAFT("minecraftUser", "minecraftID", "m",
            id -> { UUID uuid; return (uuid = BSUtils.getUUID(id)) != null ? uuid.toString() : null; },
            id -> BSUtils.getUUID(id) != null,
            BSUtils::getUserIDByMinecraft,
            BSUtils::getUserIDByMinecraftIDAndCreateIfAbsent);

    private final String databaseTableName, columnName, identifier;

    private final Function<String, String> platformIDFunction;

    private final Predicate<String> platformIDIsValid;

    private final Function<String, Long> getUserIDFunction;

    private final BiFunction<String, String, Long> getUserIDAndCreateIfAbsentFunction;

    Platform(String databaseTableName, String columnName, String identifier,
             Function<String, String> platformIDFunction,
             Predicate<String> platformIDIsValid,
             Function<String, Long> getUserIDFunction,
             BiFunction<String, String, Long> getUserIDAndCreateIfAbsentFunction) {

        this.databaseTableName = databaseTableName;
        this.columnName = columnName;
        this.identifier = identifier;

        this.platformIDFunction = platformIDFunction;
        this.platformIDIsValid = platformIDIsValid;
        this.getUserIDFunction = getUserIDFunction;
        this.getUserIDAndCreateIfAbsentFunction = getUserIDAndCreateIfAbsentFunction;
    }

    public static Platform getPlatform(String str) {
        for (Platform platform : Platform.values()) {
            if (platform.name().equalsIgnoreCase(str)) {
                return platform;
            }
        }
        return null;
    }

    public static Platform getByIdentifier(String identifier) {
        return Arrays.stream(Platform.values()).filter(p -> p.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    public static List<UUID> convertMinecraftIDs(List<String> list) {
        return list.stream().map(Platform::getMinecraftUUID).collect(Collectors.toList());
    }

    public static List<Long> convertDiscordIDs(List<String> list) {
        return list.stream().map(Platform::getDiscordID).collect(Collectors.toList());
    }

    public static UUID getMinecraftUUID(String id) {
        if (id == null) return null;
        return UUID.fromString(id);
    }

    public static long getDiscordID(String id) {
        if (id == null) return -1L;
        return Long.parseLong(id);
    }

    public boolean platformIDIsValid(String platformID) {
        return this.platformIDIsValid.test(platformID);
    }

    public boolean userExists(String platformID) {
        return this.getUserID(platformID) >= 0;
    }

    public long getUserID(String platformID) {
        return this.getUserIDFunction.apply(platformID);
    }

    // Returns null if doesn't exist
    public String getPlatformID(String platformID) {
        return this.platformIDFunction.apply(platformID);
    }

    public long getUserIDAncCreateIfAbsent(String platformID, String permissionOnAbsent) {
        return this.getUserIDAndCreateIfAbsentFunction.apply(platformID, permissionOnAbsent);
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

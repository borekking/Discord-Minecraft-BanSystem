package de.borekking.banSystem.util;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.util.discord.DiscordUtils;
import de.borekking.banSystem.util.minecraft.MinecraftUUIDUtils;

import java.util.UUID;

import net.dv8tion.jda.api.entities.User;

public final class BSUtils {

    private  BSUtils() {
    }

    // Get discord user ID by String containing discord id or discord tag.
    public static long getDiscordID(String discordID) {
        // 1. Try with discord id
        User discordUserA = DiscordUtils.getUserByID(discordID);

        if (discordUserA != null) return discordUserA.getIdLong();

        // 2. Try with discord tag
        User discordUserB = DiscordUtils.getUserByTag(discordID);

        if (discordUserB != null) return discordUserB.getIdLong();

        // 3. Return -1 as illegal id
        return -1L;
    }

    public static long getUserIDByDiscord(String discordID) {
        long discordIDLong = getDiscordID(discordID);
        if (discordIDLong == -1L) return -1L;

        String discordIDStr = String.valueOf(discordIDLong);
        return BungeeMain.getUserID(Platform.DISCORD, discordIDStr);

    }

    // Get userID (sql, for punishments, ...) from Discord
    // Platform String (Either discordID or Tag).
    public static long getUserIDByDiscordIDAndCreateIfAbsent(String discordID, String permissionsOnAbsent) {
        long discordIDLong = getDiscordID(discordID);
        if (discordIDLong == -1L) return -1L;

        String discordIDStr = String.valueOf(discordIDLong);
        return BungeeMain.getInstance().getUserManager().getAndCreateIfAbsent(Platform.DISCORD, discordIDStr, permissionsOnAbsent);
    }

    // Get uuid (UUID) from minecraft uuid or minecraft name (as String)
    public static UUID getUUID(String minecraftID) {
        // Get uuid from playerIdentifier
        UUID uuid = MinecraftUUIDUtils.getUUID(minecraftID);

        if (uuid == null) {
            try {
                uuid = MinecraftUUIDUtils.getUUIDFromName(minecraftID);
            } catch (MinecraftUUIDUtils.NoSuchPlayerException ignored) {
            }
        }

        return uuid;
    }

    public static long getUserIDByMinecraft(String minecraftID) {
        UUID uuid = getUUID(minecraftID);
        if (uuid == null) return -1L;

        String uuidStr = uuid.toString();
        return BungeeMain.getUserID(Platform.MINECRAFT, uuidStr);
    }

    public static long getUserIDByMinecraftIDAndCreateIfAbsent(String minecraftID, String permissionsOnAbsent) {
        UUID uuid = getUUID(minecraftID);
        if (uuid == null) return -1L;

        String uuidStr = uuid.toString();
        return BungeeMain.getInstance().getUserManager().getAndCreateIfAbsent(Platform.MINECRAFT, uuidStr, permissionsOnAbsent);
    }

    public static String getPlatformID(Platform platform, String platformID) {
        return platform.getPlatformID(platformID);
    }
}

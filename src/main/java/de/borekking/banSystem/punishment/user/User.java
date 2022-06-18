package de.borekking.banSystem.punishment.user;

import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.util.discord.DiscordUtils;
import de.borekking.banSystem.util.minecraft.MinecraftUUIDUtils;
import java.util.List;
import java.util.UUID;

public class User {

    // Class holding information about user (obtained by DB):
    //    userId, permissions, MC-UUIDs, discordIDs,

    private final long id;

    private final String permissions;

    private final List<UUID> uuids;

    private final List<Long> discordIDs;

    public User(long id, String permissions, List<String> uuids, List<String> discordIDs) {
        this.id = id;
        this.permissions = permissions;
        this.uuids = Platform.convertMinecraftIDs(uuids);
        this.discordIDs = Platform.convertDiscordIDs(discordIDs);
    }

    public String getName() {
        if (this.uuids.size() > 0) {
            return MinecraftUUIDUtils.getNameFromUUID(uuids.get(0));
        }

        if (this.discordIDs.size() > 0) {
            return DiscordUtils.getUserByID(this.discordIDs.get(0)).getName();
        }

        return "";
    }

    public long getId() {
        return id;
    }

    public String getPermissions() {
        return permissions;
    }

    public List<UUID> getUuids() {
        return uuids;
    }

    public List<Long> getDiscordIDs() {
        return discordIDs;
    }
}

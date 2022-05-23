package de.borekking.banSystem.punishment.user;

import de.borekking.banSystem.punishment.Platform;
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

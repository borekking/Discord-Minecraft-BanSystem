package de.borekking.banSystem.config.autoReason;

import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.punishment.Punishment;

public class AutoReason {

    // Class for holding information about the
    // auto reasons/options (duration, reason).

    private final int id;

    private final long duration;

    private final String name;

    public AutoReason(int id, long duration, String name) {
        this.id = id;
        this.duration = duration;
        this.name = name;
    }

    public Punishment createPunishment(long userID, long operatorID, Platform platform) {
        long timestampStart = System.currentTimeMillis();
        long timestampEnd = this.duration < 0L ? -1L : timestampStart + this.duration;

        return new Punishment(userID, operatorID, timestampStart, timestampEnd, platform, this.name);
    }

    public int getId() {
        return id;
    }

    public long getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }
}

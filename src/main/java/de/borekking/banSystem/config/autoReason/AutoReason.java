package de.borekking.banSystem.config.autoReason;

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

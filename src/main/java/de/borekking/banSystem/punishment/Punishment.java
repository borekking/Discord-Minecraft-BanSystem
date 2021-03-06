package de.borekking.banSystem.punishment;

public class Punishment {

    // Class holding information about a Punishment (e.g. Ban, Mute)

    private final long userID, timestamp, timestampEnd;

    private long operatorID;

    private final Platform platform;

    private final String reason;

    public Punishment(long userID, long operatorID, long timestamp, long timestampEnd, Platform platform, String reason) {
        this.userID = userID;
        this.operatorID = operatorID;
        this.timestamp = timestamp;
        this.timestampEnd = timestampEnd;
        this.platform = platform;
        this.reason = reason;
    }

    public long getUserID() {
        return userID;
    }

    public long getOperatorID() {
        return operatorID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTimestampEnd() {
        return timestampEnd;
    }

    public long getDuration() {
        return timestampEnd - timestamp;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getReason() {
        return reason;
    }

    public void setOperatorID(long operatorID) {
        this.operatorID = operatorID;
    }
}

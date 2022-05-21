package de.borekking.banSystem.punishment;

public interface IPunishHandler {

    // Check if user is punished
    boolean isPunished(long userID, Platform platform);

    // Punish a user if user exists (overrides older punishments)
    void punish(Punishment punishment, Platform platform);

    // Get current punishment for given userID
    Punishment getPunishment(long userID, Platform platform);

    // Remove punishment long userID
    void unPunish(long userID, Platform platform);

    // If user is punished but timestampEnd is over
    boolean isOver(long userID, Platform platform);
}

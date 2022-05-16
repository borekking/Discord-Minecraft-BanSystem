package de.borekking.banSystem.punishment;

public interface IPunishHandler {

    // Check if user is punished
    boolean isPunished(long userID);

    // Punish a user if user exists (overrides older punishments)
    void punish(Punishment punishment);

    // Get current punishment for given userID
    Punishment getPunishment(long userID);

    // Remove punishment long userID
    void unPunish(long userID);

    // If user is punished but timestampEnd is over
    boolean isOver(long userID);
}

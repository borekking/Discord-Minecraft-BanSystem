package de.borekking.banSystem.punishment;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.sql.SQLClient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GeneralPunishmentHandler implements IPunishHandler {

    // Class handling sql stuff for "General Punishments": Bans, Mutes.
    // Provides methods to punish, un-punish, get a punishment, check for punishment, check if punishment is over.

    // Database table:
    //    userId (BIGINT), operatorId (BIGINT), platform (VARCHAR(2)), timestamp (BIGINT), timestamp-end (BIGINT), reason (VARCHAR)

    // Table and Column Names (for DB)
    private final String tableName, userIDName = "userId", operatorIDName = "operatorId",
            platformName = "platform", timestampName = "timestamp", timestampEndName = "timestampEnd", reasonName = "reason";

    private final SQLClient database;

    private final PreparedStatement isPunishedStatement, // PS if user is punished (1 -> userID, 2 -> Platform)
                                    punishStatement, // PS for punishing a user (...)
                                    getPunishStatement, // PS for getting a punishment from userID (1 -> userID, 2 -> Platform)
                                    deletePunishmentStatement; // PS for deleting a punishment (1 -> userID, 2 -> Platform)

    public GeneralPunishmentHandler(SQLClient database, String tableName) {
        this.database = database;
        this.tableName = tableName;

        this.createDBTable();

        this.isPunishedStatement = this.database.getPreparedStatement("SELECT " + this.userIDName + " FROM " + this.tableName + " WHERE " + this.userIDName + " = ? AND " + this.platformName + " = ?;");
        this.punishStatement = this.database.getPreparedStatement("INSERT INTO " + this.tableName + " (" + this.userIDName + ", " + this.operatorIDName + ", " + this.platformName +
                ", " + this.timestampName + ", " + this.timestampEndName + ", " + this.reasonName + ") VALUES(?, ?, ?, ?, ?, ?);");
        this.getPunishStatement = this.database.getPreparedStatement("SELECT * FROM " + this.tableName + " WHERE " + this.userIDName + " = ? AND " + this.platformName + " = ?;");
        this.deletePunishmentStatement = this.database.getPreparedStatement("DELETE FROM " + this.tableName + " WHERE " + this.userIDName + " = ? AND " + this.platformName + " = ?;");
    }

    @Override
    // If user is banned
    public boolean isPunished(long userID, Platform platform) {
        try {
            this.isPunishedStatement.setLong(1, userID);
            this.isPunishedStatement.setString(2, platform.getIdentifier());
        } catch (SQLException e) {
            this.handleSQLError(e);
            return false;
        }

        return this.database.preparedStatementHasResult(this.isPunishedStatement);
    }

    @Override
    public void punish(Punishment punishment, Platform platform) {
        try {
            this.punishStatement.setLong(1, punishment.getUserID());
            this.punishStatement.setLong(2, punishment.getOperatorID());
            this.punishStatement.setString(3, punishment.getPlatform().getIdentifier());
            this.punishStatement.setLong(4, punishment.getTimestamp());
            this.punishStatement.setLong(5, punishment.getTimestampEnd());
            this.punishStatement.setString(6, punishment.getReason());
        } catch (SQLException e) {
            this.handleSQLError(e);
            return;
        }

        this.database.update(this.punishStatement);
    }

    @Override
    public Punishment getPunishment(long userID, Platform platform) {
        try {
            this.isPunishedStatement.setLong(1, userID);
            this.isPunishedStatement.setString(2, platform.getIdentifier());
        } catch (SQLException e) {
            this.handleSQLError(e);
            return null;
        }

        ResultSet resultSet = this.database.getQuery(this.getPunishStatement);

        try {
            // Set ResultSet to first result
            resultSet.next();

            return new Punishment(
                    resultSet.getLong(this.userIDName),
                    resultSet.getLong(this.operatorIDName),
                    resultSet.getLong(this.timestampName),
                    resultSet.getLong(this.timestampEndName),
                    Platform.getByIdentifier(resultSet.getString(this.platformName)),
                    resultSet.getString(this.reasonName)
            );
        } catch (SQLException e) {
            this.handleSQLError(e);
            return null;
        }
    }

    @Override
    public void unPunish(long userID, Platform platform) {
        try {
            this.isPunishedStatement.setLong(1, userID);
            this.isPunishedStatement.setString(2, platform.getIdentifier());
        } catch (SQLException e) {
            this.handleSQLError(e);
            return;
        }

        this.database.update(this.deletePunishmentStatement);
    }

    @Override
    public boolean isOver(long userID, Platform platform) {
        Punishment punishment = this.getPunishment(userID, platform);
        return System.currentTimeMillis() >= punishment.getTimestampEnd();
    }

    private void createDBTable() {
        // DB should never be not connected here (Auto-Shutdown in SQLClient class for this case)
        // If it is, do shutdown here.
        if (!this.database.isConnected()) {
            BungeeMain.sendErrorMessage("Connection to MySQL-Database lost!Stopping server...");
            BungeeMain.shutdown();
        }

        // Create table with columns:
        //    user-id (BIGINT), platform ("m" or "d" -> max. 2 chars String), timestamp (BIGINT),
        //    timestamp-end (BIGINT), reason (max. 8000 chars String)
        this.database.update("CREATE TABLE IF NOT EXISTS " + this.tableName + " (" + this.userIDName + " BIGINT, " + this.operatorIDName + " BIGINT, " + this.platformName +
                " VARCHAR(2), " + this.timestampName + " BIGINT, " + this.timestampEndName + " BIGINT, " + this.reasonName + " VARCHAR(8000));");
    }

    private void handleSQLError(SQLException e) {
        e.printStackTrace();
        BungeeMain.sendErrorMessage("SQL Error accord! Stopping server...");
        BungeeMain.shutdown();
    }
}

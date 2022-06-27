package de.borekking.banSystem.punishment;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.sql.SQLClient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeneralPunishmentHandler implements IPunishHandler {

    // Class handling sql stuff for "General Punishments": Bans, Mutes.
    // Provides methods to punish, un-punish, get a punishment, check for punishment, check if punishment is over.

    // Database table:
    //    userId (BIGINT), operatorId (BIGINT), platform (VARCHAR(2)), timestamp (BIGINT), timestamp-end (BIGINT), reason (VARCHAR)

    // Table and Column Names (for DB)
    private final String tableName, archiveName, userIDName = "userId", operatorIDName = "operatorId",
            platformName = "platform", timestampName = "timestamp", timestampEndName = "timestampEnd", reasonName = "reason";


    private final SQLClient database;

    private final PreparedStatement isPunishedStatement, // PS if user is punished (1 -> userID, 2 -> Platform)
                                    punishStatement, // PS for punishing a user (...)
                                    getPunishStatement, // PS for getting a punishment from userID (1 -> userID, 2 -> Platform)
                                    deletePunishmentStatement, // PS for deleting a punishment (1 -> userID, 2 -> Platform)
                                    getAllPunishmentsStatement, // PS for getting RS with all userIDs in Punish-Table ()

                                    getArchivedPunishmentsStatement, // PS for getting RS with all of a user's archived punishments (1 -> userID)
                                    addArchivedPunishmentStatement; // PS for adding an archived punishment (...)


    // Type of Punishment to handle
    private final PunishmentType punishmentType;

    public GeneralPunishmentHandler(SQLClient database, String tableName, PunishmentType punishmentType) {
        this.database = database;
        this.tableName = tableName;
        this.archiveName = this.tableName + "archive";
        this.punishmentType = punishmentType;

        this.createDBTable();

        // Create main table (tableName) PS
        this.isPunishedStatement = this.database.getPreparedStatement("SELECT " + this.userIDName + " FROM " + this.tableName + " WHERE " + this.userIDName + " = ? AND " + this.platformName + " = ?;");
        this.punishStatement = this.database.getPreparedStatement("INSERT INTO " + this.tableName + " (" + this.userIDName + ", " + this.operatorIDName + ", " + this.platformName +
                ", " + this.timestampName + ", " + this.timestampEndName + ", " + this.reasonName + ") VALUES(?, ?, ?, ?, ?, ?);");
        this.getPunishStatement = this.database.getPreparedStatement("SELECT * FROM " + this.tableName + " WHERE " + this.userIDName + " = ? AND " + this.platformName + " = ?;");
        this.deletePunishmentStatement = this.database.getPreparedStatement("DELETE FROM " + this.tableName + " WHERE " + this.userIDName + " = ? AND " + this.platformName + " = ?;");
        this.getAllPunishmentsStatement = this.database.getPreparedStatement("SELECT * FROM " + this.tableName + ";");

        // Create archived table (archiveName) PS
        this.getArchivedPunishmentsStatement = this.database.getPreparedStatement("SELECT * FROM " + this.archiveName + " WHERE " + this.userIDName + " = ?;");
        this.addArchivedPunishmentStatement = this.database.getPreparedStatement("INSERT INTO " + this.archiveName + " (" + this.userIDName + ", " + this.operatorIDName + ", " + this.platformName +
                ", " + this.timestampName + ", " + this.timestampEndName + ", " + this.reasonName + ") VALUES(?, ?, ?, ?, ?, ?);");

        this.startPunishmentOverCheck();
    }

    @Override
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
    public void punish(Punishment punishment) {
        this.punish(punishment, true);
    }

    private void punish(Punishment punishment, boolean broadcast) {
        // Before punishing: Delete old punishments
        this.unPunish(punishment, false, false);

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

        if (broadcast) {
            this.punishmentType.punish(punishment);
            this.punishmentType.broadcastPunishment(punishment);
        }

        this.database.update(this.punishStatement);
    }

    @Override
    public Punishment getPunishment(long userID, Platform platform) {
        try {
            this.getPunishStatement.setLong(1, userID);
            this.getPunishStatement.setString(2, platform.getIdentifier());
        } catch (SQLException e) {
            this.handleSQLError(e);
            return null;
        }

        ResultSet resultSet = this.database.getQuery(this.getPunishStatement);
        return this.getPunishment(resultSet);
    }

    @Override
    public void unPunish(Punishment punishment) {
        this.unPunish(punishment, true, true);
    }

    private void unPunish(Punishment punishment, boolean broadcast, boolean archive) {
        try {
            this.deletePunishmentStatement.setLong(1, punishment.getUserID());
            this.deletePunishmentStatement.setString(2, punishment.getPlatform().getIdentifier());
        } catch (SQLException e) {
            this.handleSQLError(e);
            return;
        }

        if (broadcast) {
            this.punishmentType.broadcastUnPunishment(punishment);
            this.punishmentType.unPunish(punishment);
        }

        this.database.update(this.deletePunishmentStatement);

        if (archive) {
            this.archivePunishment(punishment);
        }
    }

    @Override
    public List<Punishment> getAllPunishments() {
        ResultSet userIDs = this.database.getQuery(this.getAllPunishmentsStatement);

        List<Punishment> punishments = new ArrayList<>();

        Punishment punishment;
        while ((punishment = this.getPunishment(userIDs)) != null) {
            punishments.add(punishment);
        }

        return punishments;
    }

    @Override
    public List<Punishment> getOldPunishments(long userID) {
        try {
            this.getArchivedPunishmentsStatement.setLong(1, userID);
        } catch (SQLException e) {
            this.handleSQLError(e);
            return null;
        }

        ResultSet userIDs = this.database.getQuery(this.getArchivedPunishmentsStatement);

        List<Punishment> punishments = new ArrayList<>();

        Punishment punishment;
        while ((punishment = this.getPunishment(userIDs)) != null) {
            punishments.add(punishment);
        }

        return punishments;
    }

    public boolean isOver(Punishment punishment) {
        long timestampEnd = punishment.getTimestampEnd();
        if (timestampEnd < 0L) return false;

        return System.currentTimeMillis() >= timestampEnd;
    }

    private void archivePunishment(Punishment punishment) {
        try {
            this.addArchivedPunishmentStatement.setLong(1, punishment.getUserID());
            this.addArchivedPunishmentStatement.setLong(2, punishment.getOperatorID());
            this.addArchivedPunishmentStatement.setString(3, punishment.getPlatform().getIdentifier());
            this.addArchivedPunishmentStatement.setLong(4, punishment.getTimestamp());
            this.addArchivedPunishmentStatement.setLong(5, punishment.getTimestampEnd());
            this.addArchivedPunishmentStatement.setString(6, punishment.getReason());
        } catch (SQLException e) {
            this.handleSQLError(e);
            return;
        }

        this.database.update(this.addArchivedPunishmentStatement);
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
        String str = "CREATE TABLE IF NOT EXISTS %s (" + this.userIDName + " BIGINT, " + this.operatorIDName + " BIGINT, " + this.platformName +
                " VARCHAR(2), " + this.timestampName + " BIGINT, " + this.timestampEndName + " BIGINT, " + this.reasonName + " VARCHAR(8000));";
        this.database.update(String.format(str, this.tableName));
        this.database.update(String.format(str, this.archiveName));
    }

    private void startPunishmentOverCheck() {
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(10_000); // 10 seconds
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Get all punishments, filter for those which are over and call unPunish with them
                List<Punishment> punishments = this.getAllPunishments();
                List<Punishment> punishmentsOver = punishments.stream().filter(this::isOver).collect(Collectors.toList());
                punishmentsOver.forEach(this::unPunish);
            }
        }).start();
    }

    // Return current entry of RS as Punishment
    private Punishment getPunishment(ResultSet resultSet) {
        try {
            // Set ResultSet to first result and check if there is one
            // if not so, result null.
            if (!resultSet.next()) {
                return null;
            }

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

    private void handleSQLError(SQLException e) {
        e.printStackTrace();
        BungeeMain.sendErrorMessage("SQL Error accord! Stopping server...");
        BungeeMain.shutdown();
    }

    public String getName() {
        return tableName;
    }
}

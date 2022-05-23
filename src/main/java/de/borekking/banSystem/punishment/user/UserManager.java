package de.borekking.banSystem.punishment.user;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.sql.SQLClient;
import de.borekking.banSystem.util.RandomNumberCreator;
import de.borekking.banSystem.util.functional.SQLExceptionConsumer;
import de.borekking.banSystem.util.functional.SQLExceptionRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager {

    // Class for managing users in database.
    // Methods to create and merge users.

    // Database tables:
    //    minecraftUser:
    //       userId (BIGINT), minecraftID (String)
    //    discordUser:
    //       userID (BIGINT), discordID (String)
    //    user:
    //       userID (BIGINT), permissions (String)

    // Overview:
    //    A user can (so far) have MC-UUIDs and DC-IDs. Every different id is stored in a different DB, s.t.
    //    new ones can be added easily. Each of those DBs stores one user-id and the associated id per row.
    //    Each user is stored in the User table (one per row). There also the permissions are stored as String.

    // Merging:
    //    On merging two userIDs a and b, for each Platform-Table, program check for id b and set it to id a.
    //    In User-Table id b will be deleted and permissions from a and b will be merged into a.
    //    Know only id a exists anymore.

    // Permissions:
    //    Permissions are stored in User-Table.
    //    Permissions, e.g.: "reload.mc;ban.mc.*"

    private final SQLClient database;

    private final String userTableName = "user", userIDName = "userID", permissionsName = "permissions";

    private final PreparedStatement addUserToUser, // (1 -> userID, 2 -> permissions)
                                    getAllFromUser, // (1 -> userID)
                                    removeFromUser, // (1 -> userID)
                                    updateUserPermissions; // (1 -> permissions, 2 -> userID)

    private final Map<Platform, PreparedStatement>
            addToPlatformTable, // For All: (1 -> userID, 2 -> platformID)
            getUserIDFromPlatform; // For All: (1 -> platformID)


    public UserManager(SQLClient database) {
        this.database = database;

        this.createDBTables();

        this.addUserToUser = this.database.getPreparedStatement("INSERT INTO " + this.userTableName + " (?, ?);");
        this.getAllFromUser = this.database.getPreparedStatement("SELECT * FROM " + this.userTableName + " WHERE " + this.userIDName + " = ?;");
        this.removeFromUser = this.database.getPreparedStatement("DELETE FROM " + this.userTableName + " WHERE " + this.userIDName + " = ?;");
        this.updateUserPermissions = this.database.getPreparedStatement("UPDATE " + this.userTableName + " SET " + this.permissionsName + " = ? WHERE " + this.userIDName + " = ?;");

        this.addToPlatformTable = new HashMap<>();
        this.setAddToPlatformTablePS();

        this.getUserIDFromPlatform = new HashMap<>();
        this.setGetUserIDFromPlatformPS();
    }

    public void addUser(Platform platform, String platformId, String permission) {
        long userID = this.getUserID(platform, platformId);

        // Check platform details do not exist yet
        if (userID < 0) {
            userID = this.getNewUserID();

            long finalUserID = userID;
            if (!this.setPSValuesWrapper(this.addUserToUser, statement -> {
                this.addUserToUser.setLong(1, finalUserID);
                this.addUserToUser.setString(2, permission);
            })) {
                return;
            }

            PreparedStatement addToPlatform = this.addToPlatformTable.get(platform);

            if (!this.setPSValuesWrapper(addToPlatform, statement -> {
                addToPlatform.setLong(1, finalUserID);
                addToPlatform.setString(2, platformId);
            })) {
                return;
            }

            this.database.update(this.addUserToUser);
            this.database.update(addToPlatform);
        }
    }

    public long getUserID(Platform platform, String platformId) {
        PreparedStatement statement = this.getUserIDFromPlatform.get(platform);

        if (!this.setPSValuesWrapper(statement, statement1 -> statement1.setString(1, platformId))) {
            return -1L;
        }

        ResultSet rs = this.database.getQuery(statement);

        try {
            // Set ResultSet to first result and check if there is one
            // if not so, result null.
            if (!rs.next()) {
                return 1L;
            }

            return rs.getLong(this.userIDName);
        } catch (SQLException e) {
            this.handleSQLError(e);
            return -1L;
        }
    }

    // Merging:
    //    On merging two userIDs a and b, for each Platform-Table, program check for id b and set it to id a.
    //    In User-Table id b will be deleted and permissions from a and b will be merged into a.
    //    Know only id a exists anymore.
    public void merge(long userIDA, long userIDB) {
        // Check if both userIDs exist
        if (!this.userIDExists(userIDA) || !this.userIDExists(userIDB)) {
            return;
        }

        // Get Users
        User userA = this.getUser(userIDA);
        User userB = this.getUser(userIDB);

        // Merge permissions
        String newPermissions = userA.getPermissions(); // TODO!

        // Set new permissions for userA
        if (!this.setPSValuesWrapper(this.updateUserPermissions, statement -> {
            statement.setString(1, newPermissions);
            statement.setLong(2, userIDA);
        })) { return; }

        if (!this.safeRunSQL(this.updateUserPermissions::execute)) { return; }

        // Delete userB in User-Table
        if (!this.setPSValuesWrapper(this.removeFromUser, statement -> statement.setLong(1, userIDB))) {
            return;
        }

        if (!this.safeRunSQL(this.removeFromUser::execute)) { return; }

        // For all Platforms: Update userIDB to userIDA
        for (Platform platform : Platform.values()) {
            String qry = "UPDATE " + platform.getDatabaseTableName() + " SET " + this.userIDName + " = " + userIDA + " WHERE " + this.userIDName + " = " + userIDB + ";";
            this.database.update(qry);
        }
    }

    public User getUser(long userID) {
        if (!this.setPSValuesWrapper(this.getAllFromUser, statement -> statement.setLong(1, userID))) {
            return null;
        }

        ResultSet userRS = this.database.getQuery(this.getAllFromUser);
        String permissions;

        try {
            // Set ResultSet to first result and check if there is one
            // if not so, result null.
            if (!userRS.next()) {
                return null;
            }

            permissions = userRS.getString(this.permissionsName);
        } catch (SQLException e) {
            this.handleSQLError(e);
            return null;
        }

        List<String> minecraftIDs = this.getPlatformIDs(userID, Platform.MINECRAFT);
        List<String> discordIDs = this.getPlatformIDs(userID, Platform.DISCORD);

        return new User(userID, permissions, minecraftIDs, discordIDs);
    }

    public boolean userIDExists(long userID) {
        if (!this.setPSValuesWrapper(this.getAllFromUser, statement -> statement.setLong(1, userID))) {
            return false;
        }

        return this.database.preparedStatementHasResult(this.getAllFromUser);
    }

    private List<String> getPlatformIDs(long userID, Platform platform) {
        String query = "SELECT " + platform.getColumnName() + " FROM " + platform.getDatabaseTableName()
                + " WHERE " + this.userIDName + " = " + userID + ";";

        List<String> platformIDs = new ArrayList<>();
        ResultSet rs = this.database.getQuery(query);

        if (!this.safeRunSQL(() -> {
            while (rs.next()) {
                String platformID = rs.getString(platform.getColumnName());
                platformIDs.add(platformID);
            }
        })) { return null; }

        return platformIDs;
    }

    private void setAddToPlatformTablePS() {
        for (Platform platform : Platform.values()) {
            PreparedStatement statement = this.database.getPreparedStatement(
                    "INSERT INTO " + platform.getDatabaseTableName() + " (?, ?);"
            );

            this.addToPlatformTable.put(platform, statement);
        }
    }

    private void setGetUserIDFromPlatformPS() {
        for (Platform platform : Platform.values()) {
            PreparedStatement statement = this.database.getPreparedStatement(
                    "SELECT " + this.userIDName + " FROM " + platform.getDatabaseTableName() + " WHERE " + platform.getColumnName() + " = ?;"
            );

            this.getUserIDFromPlatform.put(platform, statement);
        }
    }

    // Creates all tables as explained above.
    private void createDBTables() {
        // DB should never be not connected here (Auto-Shutdown in SQLClient class for this case)
        // If it is, do shutdown here.
        if (!this.database.isConnected()) {
            BungeeMain.sendErrorMessage("Connection to MySQL-Database lost! Stopping server...");
            BungeeMain.shutdown();
        }

        // 1. Create user table with userIDs and permissions
        this.database.update("CREATE TABLE IF NOT EXISTS " + this.userTableName + " (" + this.userIDName + " BIGINT, " + this.permissionsName + " VARCHAR(8000));");

        // 2. For each Platform, create table by given attributes in Platform
        for (Platform platform : Platform.values()) {
            this.database.update("CREATE TABLE IF NOT EXISTS " + platform.getDatabaseTableName() + " (" + this.userIDName + " BIGINT, " + platform.getColumnName() + " VARCHAR(100));");
        }
    }

    private long getNewUserID() {
        long userID;

        do {
            userID = RandomNumberCreator.getRandomLong();
        } while (!this.userIDExists(userID));

        return userID;
    }

    private boolean setPSValuesWrapper(PreparedStatement statement, SQLExceptionConsumer<PreparedStatement> consumer) {
        try {
            consumer.accept(statement);
            return true;
        } catch (SQLException e) {
            this.handleSQLError(e);
            return false;
        }
    }

    private boolean safeRunSQL(SQLExceptionRunnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (SQLException e) {
            this.handleSQLError(e);
            return false;
        }
    }

    private void handleSQLError(SQLException e) {
        e.printStackTrace();
        BungeeMain.sendErrorMessage("SQL Error accord! Stopping server...");
        BungeeMain.shutdown();
    }
}
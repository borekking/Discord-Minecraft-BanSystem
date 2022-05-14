package de.borekking.banSystem.sql;

import de.borekking.banSystem.BungeeMain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLClient {

    // Class representing a database and holding the actual connection.
    // Methods Prepared Statements, Queries, Updates, closing and connecting.

    private boolean connected;
    private Connection connection;
    private final String host, database, user, password;

    public SQLClient(String host, String database, String user, String password) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;

        this.connect();
    }

    public void close() {
        if (this.connection == null)
            return;

        try {
            this.connection.close();
        } catch (SQLException ignored) {
        }

        this.connected = false;
    }

    public void connect() {
        try {
            String url = "jdbc:mysql://" + this.host + ":3306/" + this.database + "?autoReconnect=true";
            this.connection = DriverManager.getConnection(url, this.user, this.password);
            this.connected = true;
        } catch (SQLException e) {
            e.printStackTrace();
            BungeeMain.sendErrorMessage("MYSQL USERNAME, IP ODER PASSWORT FALSCH! -> Disabled");
            BungeeMain.shutdown();
        }
    }

    public ResultSet getQuery(String qry) {
        ResultSet rs = null;

        try {
            Statement st = this.connection.createStatement();
            rs = st.executeQuery(qry);
        } catch (SQLException e) {
            e.printStackTrace();
            BungeeMain.sendErrorMessage("Connection to MySQL-Database lost!");
            this.connect(); // Try to connect again
        }

        return rs;
    }

    public void update(String qry) {
        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(qry);
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            BungeeMain.sendErrorMessage("Connection to MySQL-Database lost!");
            this.connect(); // Try to connect again
        }
    }

    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        return this.connection.prepareStatement(sql);
    }

    public boolean isConnected() {
        return connected;
    }
}

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

    public PreparedStatement getPreparedStatement(String sql) {
        try {
            return this.connection.prepareStatement(sql);
        } catch (SQLException e) {
            this.catchSQLException(e);
            return this.getPreparedStatement(sql);
        }
    }

    public ResultSet getQuery(PreparedStatement statement) {
        ResultSet rs = null;

        try {
            rs = statement.executeQuery();
        } catch (SQLException e) {
            this.catchSQLException(e);
        }

        return rs;
    }

    public ResultSet getQuery(String qry) {
        ResultSet rs = null;

        try {
            Statement st = this.connection.createStatement();
            rs = st.executeQuery(qry);
        } catch (SQLException e) {
            this.catchSQLException(e);
        }

        return rs;
    }

    // Returns true if given Statement has at least one result
    public boolean preparedStatementHasResult(PreparedStatement statement) {
        ResultSet rs = null;

        try {
            rs = statement.executeQuery();
        } catch (SQLException e) {
            this.catchSQLException(e);
        }

        return this.hasResult(rs);
    }

    // Returns true if given qry has at least one result
    public boolean queryHasResult(String qry) {
        ResultSet resultSet = this.getQuery(qry);
        return hasResult(resultSet);
    }

    public void update(String qry) {
        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(qry);
            st.close();
        } catch (SQLException e) {
            this.catchSQLException(e);
        }
    }

    public void update(PreparedStatement statement) {
        try {
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            this.catchSQLException(e);
        }
    }

    // Returns if given ResultSet has at least one result
    private boolean hasResult(ResultSet resultSet) {
        if (resultSet == null) return false;

        try {
            return resultSet.next();
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                this.catchSQLException(e);
            }
        }
    }

    private void catchSQLException(SQLException e) {
        e.printStackTrace();
        BungeeMain.sendErrorMessage("Connection to MySQL-Database lost!");
        this.connect(); // Try to connect again
    }

    public boolean isConnected() {
        return connected;
    }
}

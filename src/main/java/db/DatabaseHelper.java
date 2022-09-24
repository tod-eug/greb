package db;

import util.PropertiesProvider;

import java.sql.*;

public class DatabaseHelper {

    public static final String pattern = "yyyy-MM-dd HH:mm:ss";

    private final String url;
    private final String user;
    private final String password;

    private Connection connection;
    private Statement statement;


    public DatabaseHelper() {
        this.url = PropertiesProvider.configurationProperties.get("host");
        this.user = PropertiesProvider.configurationProperties.get("user");
        this.password = PropertiesProvider.configurationProperties.get("password");
    }

    public PreparedStatement getPreparedStatement(String sql) {
        createConnection();

        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            return preparedStatement;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not prepare statement for sql=" + sql);
        }
    }

    public void closeConnections() {
        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            statement = null;
            connection = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                closeConnections();
            }
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

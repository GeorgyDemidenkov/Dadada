package lorek;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/KioskDB";
    private static final String USER = "root";
    private static final String PASSWORD = "1qaz2wsxxsw2zaq1";
    private Connection connection;

    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Успешное подключение к базе данных.");
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Подключение к базе данных закрыто.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии подключения: " + e.getMessage());
        }
    }
}

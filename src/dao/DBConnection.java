package dao; // This matches the folder name

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/healthcare_db";
    private static final String USER = "root"; // YOUR MYSQL USERNAME
    private static final String PASSWORD = "MySQL@@9876"; // YOUR MYSQL PASSWORD

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // This driver line is crucial
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
        return conn;
    }
}
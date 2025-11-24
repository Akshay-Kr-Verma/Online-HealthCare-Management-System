package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class DBConnection {

    // BASE_URL connects to MySQL Server (to create the DB)
    // FULL_URL connects to the specific Database (to run the app)
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "healthcare_db";
    private static final String FULL_URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    private static final String USER = "root";  // MySQL username (usually 'root')

    // Store password in memory after user enters it once
    private static String password = "";
    private static boolean isPasswordSet = false;

    // -------- 1. Get Connection (Normal App Usage) --------
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // If password not set yet, this may fail and then we trigger retryConnection
            conn = DriverManager.getConnection(FULL_URL, USER, password);
        } catch (Exception e) {
            System.out.println("Connection Failed (getConnection): " + e.getMessage());
            // Ask user for password and retry for the DB URL
            conn = retryConnection(FULL_URL);
        }
        return conn;
    }

    // -------- 2. Setup Database (Call Once on Startup) --------
    public static void setupDatabase() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // First connect to MySQL SERVER (no database selected)
            try {
                conn = DriverManager.getConnection(BASE_URL, USER, password);
            } catch (SQLException e) {
                // If fails, ask for password and retry
                conn = retryConnection(BASE_URL);
            }

            if (conn != null) {
                Statement stmt = conn.createStatement();

                // Create Database if not exists
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                stmt.executeUpdate("USE " + DB_NAME);

                // Create Users Table
                String userTable = "CREATE TABLE IF NOT EXISTS users ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "username VARCHAR(50) UNIQUE, "
                        + "password VARCHAR(50), "
                        + "role VARCHAR(20))";
                stmt.executeUpdate(userTable);

                // Create Appointments Table
                String apptTable = "CREATE TABLE IF NOT EXISTS appointments ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "patient_name VARCHAR(100), "
                        + "doctor_name VARCHAR(100), "
                        + "date VARCHAR(20), "
                        + "status VARCHAR(20))";
                stmt.executeUpdate(apptTable);

                // Insert Default Admin User (if not already present)
                String admin = "INSERT IGNORE INTO users (username, password, role) "
                        + "VALUES ('admin', 'admin123', 'Admin')";
                stmt.executeUpdate(admin);

                System.out.println(">> Database verified/created successfully.");
                conn.close();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to MySQL Server.\nDatabase setup aborted.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Critical Setup Error: " + e.getMessage());
        }
    }

    // -------- Helper: Ask for password and retry specific URL --------
    private static Connection retryConnection(String targetUrl) {
        Connection conn = null;

        while (conn == null) {
            JPasswordField pf = new JPasswordField();
            int action = JOptionPane.showConfirmDialog(
                    null,
                    pf,
                    "Database Connection Failed.\nEnter MySQL Root Password:",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE
            );

            if (action != JOptionPane.OK_OPTION) {
                // User cancelled the dialog
                return null;
            }

            password = new String(pf.getPassword());
            isPasswordSet = true;

            try {
                conn = DriverManager.getConnection(targetUrl, USER, password);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Connection still failed:\n" + e.getMessage() + "\nTry again."
                );
                conn = null; // force loop to continue
            }
        }

        return conn;
    }
}

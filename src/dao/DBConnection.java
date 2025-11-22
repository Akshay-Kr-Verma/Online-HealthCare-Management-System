package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.swing.JOptionPane;
// import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class DBConnection {

    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "healthcare_db";
    private static final String FULL_URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    private static final String USER = "root";

    // We don't hardcode the password anymore. We store it in memory.
    private static String password = "";
    private static boolean passwordChecked = false;

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(FULL_URL, USER, password);
        } catch (Exception e) {
            // If connection fails, it might be a wrong password.
            // We catch the error and ask the user for the password.
            System.out.println("Connection failed. Asking user for credentials...");
            conn = promptForPasswordAndRetry();
        }
        return conn;
    }

    // --- HELPER: Ask User for Password if connection fails ---
    private static Connection promptForPasswordAndRetry() {
        // Avoid infinite loops if the user keeps cancelling
        if (passwordChecked)
            return null;

        Connection conn = null;
        try {
            // Popup Input Dialog
            JPasswordField pf = new JPasswordField();
            int okCxl = JOptionPane.showConfirmDialog(null, pf, "Enter MySQL Root Password:",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (okCxl == JOptionPane.OK_OPTION) {
                password = new String(pf.getPassword());
                passwordChecked = true; // Mark as checked so we use this password for the session

                // Try connecting again with the new password
                conn = DriverManager.getConnection(FULL_URL, USER, password);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Still cannot connect. Please check MySQL is running.");
        }
        return conn;
    }

    // --- AUTOMATIC SETUP (Also uses the dynamic password) ---
    public static void setupDatabase() {
        try {
            // Try connecting to Server (Base URL)
            Connection conn = null;
            try {
                conn = DriverManager.getConnection(BASE_URL, USER, password);
            } catch (Exception e) {
                // If initial setup fails, ask for password immediately
                promptForPasswordAndRetry();
                conn = DriverManager.getConnection(BASE_URL, USER, password);
            }

            if (conn != null) {
                Statement stmt = conn.createStatement();

                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                stmt.executeUpdate("USE " + DB_NAME);

                String userTable = "CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50) UNIQUE, " +
                        "password VARCHAR(50), " +
                        "role VARCHAR(20))";
                stmt.executeUpdate(userTable);

                String apptTable = "CREATE TABLE IF NOT EXISTS appointments (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "patient_name VARCHAR(100), " +
                        "doctor_name VARCHAR(100), " +
                        "date VARCHAR(20), " +
                        "status VARCHAR(20))";
                stmt.executeUpdate(apptTable);

                String admin = "INSERT IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'Admin')";
                stmt.executeUpdate(admin);

                System.out.println(">> Database Verified!");
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
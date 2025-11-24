package main;

import dao.DBConnection;
import gui.AdminDashboard;
import gui.DoctorDashboard;
import gui.PatientDashboard;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MainApp {
    public static void main(String[] args) {

        // FIRST: Create database + tables if needed, and ask for MySQL password
        DBConnection.setupDatabase();

        // THEN: Start the Application UI
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

// ==========================
// LOGIN SCREEN
// ==========================
class LoginFrame extends JFrame {
    JTextField userField = new JTextField(15);
    JPasswordField passField = new JPasswordField(15);

    public LoginFrame() {
        setTitle("Healthcare System Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(null); // Center on screen

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- UI Components ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(passField, gbc);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Sign Up");

        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(btnPanel, gbc);

        // --- Action Listeners ---
        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> new RegistrationFrame().setVisible(true));
    }

    void login() {
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database Connection Failed!");
                return;
            }

            String sql = "SELECT role FROM users WHERE username=? AND password=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, user);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                JOptionPane.showMessageDialog(this, "Login Successful! Welcome " + role);
                this.dispose(); // Close login window

                if (role.equalsIgnoreCase("Admin")) {
                    new AdminDashboard().setVisible(true);
                } else if (role.equalsIgnoreCase("Doctor")) {
                    new DoctorDashboard(user).setVisible(true);
                } else if (role.equalsIgnoreCase("Patient")) {
                    new PatientDashboard(user).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Unknown Role: " + role);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}

// ==========================
// REGISTRATION SCREEN
// ==========================
class RegistrationFrame extends JFrame {
    JTextField userField = new JTextField(15);
    JPasswordField passField = new JPasswordField(15);
    JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Patient", "Doctor"});

    public RegistrationFrame() {
        setTitle("New User Registration");
        setSize(350, 250);
        setLayout(new GridLayout(5, 2, 10, 10));
        setLocationRelativeTo(null);

        JLabel header = new JLabel("Create New Account", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        add(header);
        add(new JLabel(""));

        add(new JLabel("  New Username:"));
        add(userField);
        add(new JLabel("  New Password:"));
        add(passField);
        add(new JLabel("  Select Role:"));
        add(roleCombo);

        JButton registerBtn = new JButton("Create Account");
        add(new JLabel(""));
        add(registerBtn);

        registerBtn.addActionListener(e -> registerUser());
    }

    private void registerUser() {
        String newUser = userField.getText();
        String newPass = new String(passField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (newUser.isEmpty() || newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database Connection Failed!");
                return;
            }

            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, newUser);
            pst.setString(2, newPass);
            pst.setString(3, role);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Registration Successful! You can now Login.");
                this.dispose();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registration Failed: " + ex.getMessage());
        }
    }
}

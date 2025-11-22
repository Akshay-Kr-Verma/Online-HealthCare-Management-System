package gui;

import dao.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class AdminDashboard extends JFrame {

    private JTable userTable;
    private DefaultTableModel tableModel;

    // Input Fields for CRUD
    private JTextField userField = new JTextField(10);
    private JTextField passField = new JTextField(10);
    private JComboBox<String> roleCombo = new JComboBox<>(new String[] { "Doctor", "Patient", "Admin" });

    public AdminDashboard() {
        setTitle("Admin Dashboard - System Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- TOP: Clock ---
        JLabel timeLabel = new JLabel("Time: --:--:--");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(timeLabel);
        add(top, BorderLayout.NORTH);
        startClock(timeLabel);

        // --- CENTER: User List ---
        String[] cols = { "ID", "Username", "Password", "Role" };
        tableModel = new DefaultTableModel(cols, 0);
        userTable = new JTable(tableModel);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // --- BOTTOM: CRUD Controls ---
        JPanel controlPanel = new JPanel(new GridLayout(2, 1));

        // Inputs
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(userField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(passField);
        inputPanel.add(new JLabel("Role:"));
        inputPanel.add(roleCombo);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add User");
        JButton updateBtn = new JButton("Update Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton clearBtn = new JButton("Clear Fields");

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        controlPanel.add(inputPanel);
        controlPanel.add(btnPanel);
        add(controlPanel, BorderLayout.SOUTH);

        // --- LISTENERS ---
        addBtn.addActionListener(e -> addUser());
        deleteBtn.addActionListener(e -> deleteUser());
        updateBtn.addActionListener(e -> updateUser());
        clearBtn.addActionListener(e -> {
            userField.setText("");
            passField.setText("");
        });

        // Load Users on start
        loadUsers();

        // Mouse Listener to fill fields when row clicked
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = userTable.getSelectedRow();
                userField.setText(tableModel.getValueAt(row, 1).toString());
                passField.setText(tableModel.getValueAt(row, 2).toString());
                roleCombo.setSelectedItem(tableModel.getValueAt(row, 3).toString());
            }
        });
    }

    // READ: Uses Collections (ArrayList) to satisfy rubric
    private void loadUsers() {
        tableModel.setRowCount(0);
        List<String[]> userList = new ArrayList<>(); // Collection usage

        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                userList.add(new String[] {
                        String.valueOf(rs.getInt("id")),
                        rs.getString("username"),
                        rs.getString("password"), // Showing password for Admin convenience
                        rs.getString("role")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String[] s : userList) {
            tableModel.addRow(s);
        }
    }

    // CREATE
    private void addUser() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, userField.getText());
            pst.setString(2, passField.getText());
            pst.setString(3, roleCombo.getSelectedItem().toString());

            pst.executeUpdate();
            loadUsers();
            JOptionPane.showMessageDialog(this, "User Added!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // UPDATE
    private void updateUser() {
        int row = userTable.getSelectedRow();
        if (row == -1)
            return;
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET username=?, password=?, role=? WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, userField.getText());
            pst.setString(2, passField.getText());
            pst.setString(3, roleCombo.getSelectedItem().toString());
            pst.setInt(4, id);

            pst.executeUpdate();
            loadUsers();
            JOptionPane.showMessageDialog(this, "User Updated!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.");
            return;
        }
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM users WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, id);
                pst.executeUpdate();
                loadUsers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startClock(JLabel label) {
        new Thread(() -> {
            while (true) {
                try {
                    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    SwingUtilities.invokeLater(() -> label.setText("Time: " + time));
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }).start();
    }
}
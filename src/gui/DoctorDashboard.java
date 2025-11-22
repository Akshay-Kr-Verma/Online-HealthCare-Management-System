package gui;

import dao.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DoctorDashboard extends JFrame {

    private String currentDoctor;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;

    public DoctorDashboard(String username) {
        this.currentDoctor = username;

        setTitle("Doctor Portal - Dr. " + username);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- TOP: Clock & Welcome ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("  Daily Schedule Management", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel timeLabel = new JLabel("Time: --:--:--  ");
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        topPanel.add(title, BorderLayout.CENTER);
        topPanel.add(timeLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        startClock(timeLabel);

        // --- CENTER: Appointment Table ---
        String[] cols = { "ID", "Patient Name", "Date", "Status" };
        tableModel = new DefaultTableModel(cols, 0);
        appointmentTable = new JTable(tableModel);
        add(new JScrollPane(appointmentTable), BorderLayout.CENTER);

        // --- BOTTOM: Action Buttons ---
        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh List");
        JButton completeBtn = new JButton("Mark Completed");
        JButton cancelBtn = new JButton("Cancel Appointment");

        // Style
        completeBtn.setBackground(new Color(100, 149, 237)); // Cornflower Blue
        completeBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(new Color(220, 20, 60)); // Crimson
        cancelBtn.setForeground(Color.WHITE);

        btnPanel.add(refreshBtn);
        btnPanel.add(completeBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // --- ACTIONS ---
        refreshBtn.addActionListener(e -> loadAppointments());
        completeBtn.addActionListener(e -> updateStatus("Completed"));
        cancelBtn.addActionListener(e -> updateStatus("Cancelled"));

        // Load initially
        loadAppointments();
    }

    // READ: Load appointments for this specific doctor
    private void loadAppointments() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM appointments WHERE doctor_name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, currentDoctor);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("date"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPDATE: Change status of selected appointment
    private void updateStatus(String newStatus) {
        int row = appointmentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE appointments SET status=? WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, newStatus);
            pst.setInt(2, id);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Status Updated to: " + newStatus);
                loadAppointments(); // Refresh table
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update Failed: " + e.getMessage());
        }
    }

    // Multithreading Clock
    private void startClock(JLabel label) {
        new Thread(() -> {
            while (true) {
                try {
                    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    SwingUtilities.invokeLater(() -> label.setText("Time: " + time + "  "));
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }).start();
    }
}
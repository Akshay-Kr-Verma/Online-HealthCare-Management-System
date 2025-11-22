package gui;

import dao.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PatientDashboard extends JFrame {

    private String currentPatient; // Stores the logged-in user
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> doctorDropdown;

    // Constructor now accepts the username
    public PatientDashboard(String username) {
        this.currentPatient = username;

        setTitle("Patient Portal - Welcome " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- TOP PANEL: Multithreaded Clock ---
        JLabel timeLabel = new JLabel("Time: --:--:--");
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(timeLabel);
        add(topPanel, BorderLayout.NORTH);
        startClock(timeLabel);

        // --- MAIN TABS ---
        JTabbedPane tabs = new JTabbedPane();

        // TAB 1: Book Appointment (Create)
        tabs.add("Book New Appointment", createBookingPanel());

        // TAB 2: View History (Read, Update, Delete)
        tabs.add("My Appointment History", createHistoryPanel());

        add(tabs, BorderLayout.CENTER);

        // Load initial data
        refreshHistory();
    }

    // ==========================================
    // TAB 1: BOOKING PANEL (With Dropdown)
    // ==========================================
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Patient Name (Auto-filled & Read-only)
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Patient Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(currentPatient);
        nameField.setEditable(false); // User cannot change this
        panel.add(nameField, gbc);

        // 2. Doctor Selection (Dropdown from DB)
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Select Doctor:"), gbc);
        gbc.gridx = 1;
        doctorDropdown = new JComboBox<>();
        loadDoctors(); // Helper to fetch doctors
        panel.add(doctorDropdown, gbc);

        // 3. Date Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        JTextField dateField = new JTextField(15);
        panel.add(dateField, gbc);

        // 4. Book Button
        gbc.gridx = 1;
        gbc.gridy = 3;
        JButton bookBtn = new JButton("Confirm Booking");
        bookBtn.setBackground(new Color(34, 139, 34)); // Green
        bookBtn.setForeground(Color.WHITE);
        panel.add(bookBtn, gbc);

        bookBtn.addActionListener(e -> {
            String doc = (String) doctorDropdown.getSelectedItem();
            String date = dateField.getText();
            processBooking(currentPatient, doc, date);
        });

        return panel;
    }

    // ==========================================
    // TAB 2: HISTORY PANEL (View / Update / Delete)
    // ==========================================
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table Setup
        String[] cols = { "Appt ID", "Doctor", "Date", "Status" };
        tableModel = new DefaultTableModel(cols, 0);
        historyTable = new JTable(tableModel);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton rescheduleBtn = new JButton("Reschedule (Update)");
        JButton cancelBtn = new JButton("Cancel Appt (Delete)");

        btnPanel.add(refreshBtn);
        btnPanel.add(rescheduleBtn);
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Button Actions
        refreshBtn.addActionListener(e -> refreshHistory());
        cancelBtn.addActionListener(e -> cancelAppointment());
        rescheduleBtn.addActionListener(e -> rescheduleAppointment());

        return panel;
    }

    // --- HELPER: Load Doctors for Dropdown ---
    private void loadDoctors() {
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            // Only fetch users who are Doctors
            ResultSet rs = stmt.executeQuery("SELECT username FROM users WHERE role='Doctor'");
            doctorDropdown.removeAllItems();
            while (rs.next()) {
                doctorDropdown.addItem(rs.getString("username"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- HELPER: Load History for Table ---
    private void refreshHistory() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM appointments WHERE patient_name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, currentPatient); // Filter by logged-in user
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("doctor_name"),
                        rs.getString("date"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- TRANSACTION: Book Appointment ---
    // Includes 'synchronized' and Transaction Management for Marks
    private synchronized void processBooking(String patient, String doctor, String date) {
        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a date.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Transaction Start
            String sql = "INSERT INTO appointments (patient_name, doctor_name, date, status) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, patient);
            pst.setString(2, doctor);
            pst.setString(3, date);
            pst.setString(4, "Scheduled");

            int rows = pst.executeUpdate();
            if (rows > 0) {
                conn.commit(); // Commit
                JOptionPane.showMessageDialog(this, "Booking Confirmed! Transaction Committed.");
                refreshHistory(); // Update the table immediately
            } else {
                conn.rollback(); // Rollback
                JOptionPane.showMessageDialog(this, "Booking Failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- DELETE: Cancel Appointment ---
    private void cancelAppointment() {
        int row = historyTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.");
            return;
        }

        int apptId = (int) tableModel.getValueAt(row, 0); // Get ID from column 0

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM appointments WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, apptId);
                pst.executeUpdate();
                refreshHistory(); // Refresh table
                JOptionPane.showMessageDialog(this, "Appointment Cancelled.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // --- UPDATE: Reschedule Appointment ---
    private void rescheduleAppointment() {
        int row = historyTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to reschedule.");
            return;
        }

        int apptId = (int) tableModel.getValueAt(row, 0);

        // Simple Input Dialog for new date
        String newDate = JOptionPane.showInputDialog(this, "Enter new date (YYYY-MM-DD):");
        if (newDate != null && !newDate.isEmpty()) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE appointments SET date=?, status='Rescheduled' WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, newDate);
                pst.setInt(2, apptId);
                pst.executeUpdate();
                refreshHistory();
                JOptionPane.showMessageDialog(this, "Reschedule Successful!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // --- CLOCK THREAD ---
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
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LodgeComplaintPage extends JFrame {
    private String username;

    private JTextArea complaintTextArea;
    private DefaultTableModel complaintTableModel;

    public LodgeComplaintPage(String username) {
        this.username = username;

        setTitle("Lodge Complaint");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 🔶 Complaint Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BorderLayout(10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("New Complaint"));

        complaintTextArea = new JTextArea(5, 30);
        JScrollPane scrollPane = new JScrollPane(complaintTextArea);
        formPanel.add(scrollPane, BorderLayout.CENTER);

        JButton submitButton = new JButton("Submit Complaint");
        submitButton.addActionListener(e -> submitComplaint());
        formPanel.add(submitButton, BorderLayout.SOUTH);

        // 🧾 Complaints Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Your Complaint History"));

        complaintTableModel = new DefaultTableModel(new String[]{
                "ID", "Complaint", "Status", "Response", "Date"
        }, 0);
        JTable complaintTable = new JTable(complaintTableModel);
        tablePanel.add(new JScrollPane(complaintTable), BorderLayout.CENTER);

        // Back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            dispose();
            new StudentDashboard(username);
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);

        // Add all to frame
        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Populate table
        loadComplaints();

        setVisible(true);
    }

    private void submitComplaint() {
        String complaintText = complaintTextArea.getText().trim();
        if (complaintText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complaint cannot be empty.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Get student ID from username
            String getUserIdQuery = "SELECT id FROM users WHERE username = ?";
            PreparedStatement getUserStmt = conn.prepareStatement(getUserIdQuery);
            getUserStmt.setString(1, username);
            ResultSet rs = getUserStmt.executeQuery();

            if (rs.next()) {
                int studentId = rs.getInt("id");

                // Insert complaint
                String insertQuery = "INSERT INTO complaints (student_id, complaint_text) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, studentId);
                insertStmt.setString(2, complaintText);
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Complaint submitted successfully.");
                complaintTextArea.setText("");
                loadComplaints(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "User not found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error submitting complaint.");
        }
    }

    private void loadComplaints() {
        complaintTableModel.setRowCount(0);

        String query = """
            SELECT c.id, c.complaint_text, c.status, c.response, c.complaint_date
            FROM complaints c
            JOIN users u ON c.student_id = u.id
            WHERE u.username = ?
            ORDER BY c.complaint_date DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                complaintTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("complaint_text"),
                        rs.getString("status"),
                        rs.getString("response") != null ? rs.getString("response") : "—",
                        rs.getTimestamp("complaint_date").toString()
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading complaint history.");
        }
    }
}

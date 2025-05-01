import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HandleComplaintsPage extends JFrame {
    private JTable complaintTable;
    private DefaultTableModel tableModel;

    public HandleComplaintsPage() {
        setTitle("Handle Complaints");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Complaint ID", "Username", "Complaint", "Status"}, 0);
        complaintTable = new JTable(tableModel);

        fetchComplaints();

        JButton resolveButton = new JButton("Mark as Resolved");
        resolveButton.addActionListener(e -> markResolved());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            dispose();
            new AdminDashboard();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(resolveButton);
        buttonPanel.add(backButton);

        add(new JScrollPane(complaintTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void fetchComplaints() {
        // Join users and complaints to fetch usernames via student_id
        String query = "SELECT c.id, u.username, c.complaint_text, c.status " +
                "FROM complaints c " +
                "JOIN users u ON c.student_id = u.id " +
                "WHERE c.status = 'Pending'";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            tableModel.setRowCount(0); // Clear previous data

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("complaint_text"),
                        rs.getString("status")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading complaints.");
        }
    }

    private void markResolved() {
        int selectedRow = complaintTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint to resolve.");
            return;
        }

        int complaintId = (int) tableModel.getValueAt(selectedRow, 0);

        String updateQuery = "UPDATE complaints SET status = 'Resolved' WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setInt(1, complaintId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Complaint marked as resolved.");
            fetchComplaints();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error resolving complaint.");
        }
    }
}

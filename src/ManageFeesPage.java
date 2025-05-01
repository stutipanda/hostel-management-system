import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageFeesPage extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public ManageFeesPage() {
        setTitle("Manage Student Fees");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"Student ID", "Username", "Room Number", "Pending Amount"}, 0);
        table = new JTable(model);
        fetchApprovedBookings();

        JButton assignFeeButton = new JButton("Assign Fee");
        assignFeeButton.addActionListener(e -> assignFee());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            dispose();
            new AdminDashboard();
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(assignFeeButton);
        bottomPanel.add(backButton);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void fetchApprovedBookings() {
        String query = "SELECT br.student_id, u.username, r.room_number, IFNULL(f.pending_amount, 0) AS pending_amount " +
                "FROM booking_requests br " +
                "JOIN users u ON br.student_id = u.id " +
                "JOIN rooms r ON br.room_id = r.room_id " +
                "LEFT JOIN fees f ON br.student_id = f.student_id " +
                "WHERE br.status = 'Approved'";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("username"),
                        rs.getString("room_number"),
                        rs.getDouble("pending_amount")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching approved bookings.");
        }
    }

    private void assignFee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student.");
            return;
        }

        int studentId = (int) model.getValueAt(selectedRow, 0);
        String amountStr = JOptionPane.showInputDialog(this, "Enter fee amount:");
        if (amountStr == null) return;

        try {
            double amount = Double.parseDouble(amountStr);

            String upsert = "INSERT INTO fees (student_id, pending_amount) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE pending_amount = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(upsert)) {
                stmt.setInt(1, studentId);
                stmt.setDouble(2, amount);
                stmt.setDouble(3, amount);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Fee updated.");
                fetchApprovedBookings();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error.");
        }
    }
}


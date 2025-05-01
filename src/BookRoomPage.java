import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BookRoomPage extends JFrame {
    private JTable roomTable;
    private JButton bookButton, refreshButton;
    private String studentUsername;
    private DefaultTableModel model;

    public BookRoomPage(String username) {
        this.studentUsername = username;
        setTitle("Book Room");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table Model
        model = new DefaultTableModel(new String[]{"Room ID", "Room Number", "Status"}, 0);
        roomTable = new JTable(model);
        fetchAvailableRooms();

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        bookButton = new JButton("Request Booking");
        refreshButton = new JButton("Refresh");

        bookButton.addActionListener(e -> requestBooking());
        refreshButton.addActionListener(e -> fetchAvailableRooms()); // Refresh table

        buttonPanel.add(bookButton);
        buttonPanel.add(refreshButton);

        add(new JScrollPane(roomTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ✅ Updated method to reflect fresh availability
    private void fetchAvailableRooms() {
        String query = "SELECT r.room_id, r.room_number, " +
                "CASE " +
                "    WHEN EXISTS (SELECT 1 FROM booking_requests br2 WHERE br2.room_id = r.room_id AND br2.status = 'Approved' AND br2.student_id != ?) " +
                "         THEN 'Occupied' " +
                "    WHEN MAX(br.status) = 'Approved' THEN 'Approved' " +
                "    WHEN MAX(br.status) = 'Pending' THEN 'Pending' " +
                "    WHEN MAX(br.status) = 'Rejected' THEN 'Rejected' " +
                "    ELSE 'Available' " +
                "END AS status " +
                "FROM rooms r " +
                "LEFT JOIN booking_requests br ON r.room_id = br.room_id AND br.student_id = ? " +
                "GROUP BY r.room_id, r.room_number " +
                "ORDER BY r.room_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int studentId = getStudentId(studentUsername);
            stmt.setInt(1, studentId); // For EXISTS clause
            stmt.setInt(2, studentId); // For LEFT JOIN with student-specific bookings

            ResultSet rs = stmt.executeQuery();
            model.setRowCount(0); // Clear existing rows

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("room_id"),
                        rs.getString("room_number"),
                        rs.getString("status")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching rooms!");
        }
    }

    private int getStudentId(String username) {
        String query = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    private void requestBooking() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room!");
            return;
        }

        int roomId = (int) roomTable.getValueAt(selectedRow, 0);
        String status = (String) roomTable.getValueAt(selectedRow, 2);

        if (!status.equals("Available")) {
            JOptionPane.showMessageDialog(this, "You cannot book this room!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            int studentId = getStudentId(studentUsername);
            if (studentId == -1) {
                JOptionPane.showMessageDialog(this, "Student not found!");
                return;
            }

            // Check if student already has a pending or approved booking
            String checkQuery = "SELECT * FROM booking_requests WHERE student_id = ? AND status IN ('Pending', 'Approved')";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, studentId);
                ResultSet rsCheck = checkStmt.executeQuery();
                if (rsCheck.next()) {
                    JOptionPane.showMessageDialog(this, "You already have a pending or approved booking request!");
                    return;
                }
            }

            // Insert booking request
            String insertQuery = "INSERT INTO booking_requests (student_id, room_id, status) VALUES (?, ?, 'Pending')";
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, roomId);
                pstmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Booking request sent!");
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error sending request!");
        }
    }

    public static void main(String[] args) {
        new BookRoomPage("testuser");
    }
}

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageRoomsPage extends JFrame {
    private JTable requestTable;
    private DefaultTableModel requestModel;
    private JButton approveButton, rejectButton;

    public ManageRoomsPage() {
        setTitle("Manage Room Requests");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeRequestTable();
        initializeButtons();

        setVisible(true);
    }

    private void initializeRequestTable() {
        requestModel = new DefaultTableModel(new String[]{"Request ID", "Username", "Room Number", "Status"}, 0);
        requestTable = new JTable(requestModel);
        fetchRequests();

        JPanel requestPanel = new JPanel(new BorderLayout());
        requestPanel.setBorder(BorderFactory.createTitledBorder("Booking Requests"));
        requestPanel.add(new JScrollPane(requestTable), BorderLayout.CENTER);

        add(requestPanel, BorderLayout.CENTER);
    }

    private void initializeButtons() {
        approveButton = new JButton("Approve");
        rejectButton = new JButton("Reject");
        JButton viewRoomsButton = new JButton("View Rooms");
        JButton vacateRoomButton = new JButton("Vacate Room");
        JButton backButton = new JButton("Back");

        approveButton.addActionListener(e -> updateRequestStatus("Approved"));
        rejectButton.addActionListener(e -> updateRequestStatus("Rejected"));
        viewRoomsButton.addActionListener(e -> new RoomViewerWindow());
        vacateRoomButton.addActionListener(e -> vacateRoom());

        backButton.addActionListener(e -> {
            dispose(); // close current window
            new AdminDashboard(); // open admin dashboard (adjust constructor if needed)
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(viewRoomsButton);
        buttonPanel.add(vacateRoomButton);
        buttonPanel.add(backButton); // Add Back button to panel

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fetchRequests() {
        String query = """
                SELECT b.request_id, u.username, r.room_number, b.status
                FROM booking_requests b
                JOIN users u ON b.student_id = u.id
                JOIN rooms r ON b.room_id = r.room_id
                WHERE b.status = 'Pending'
                """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            requestModel.setRowCount(0);

            while (rs.next()) {
                requestModel.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("username"),
                        rs.getString("room_number"),
                        rs.getString("status")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching requests!");
        }
    }

    private void updateRequestStatus(String newStatus) {
        int selectedRow = requestTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request first!");
            return;
        }

        int requestId = (int) requestTable.getValueAt(selectedRow, 0);
        String roomNumber = (String) requestTable.getValueAt(selectedRow, 2);

        String updateRequestQuery = "UPDATE booking_requests SET status = ? WHERE request_id = ?";
        String updateRoomQuery = "UPDATE rooms SET status = ? WHERE room_number = ?";
        String checkRoomQuery = "SELECT status FROM rooms WHERE room_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement requestStmt = conn.prepareStatement(updateRequestQuery);
             PreparedStatement roomStmt = conn.prepareStatement(updateRoomQuery);
             PreparedStatement checkRoomStmt = conn.prepareStatement(checkRoomQuery)) {

            conn.setAutoCommit(false);

            // Step 1: Check if room is already booked
            checkRoomStmt.setString(1, roomNumber);
            ResultSet rs = checkRoomStmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                if ("Booked".equalsIgnoreCase(status) && "Approved".equalsIgnoreCase(newStatus)) {
                    JOptionPane.showMessageDialog(this, "Room is already booked!");
                    conn.rollback();
                    return;
                }
            }

            // Step 2: Update booking_requests table
            requestStmt.setString(1, newStatus);
            requestStmt.setInt(2, requestId);
            requestStmt.executeUpdate();

            // Step 3: If approved, mark the room as booked
            if ("Approved".equalsIgnoreCase(newStatus)) {
                roomStmt.setString(1, "Booked");
                roomStmt.setString(2, roomNumber);
                roomStmt.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Request has been " + newStatus + ".");
            fetchRequests();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating request!");
        }
    }

    private void vacateRoom() {
        String roomNumber = JOptionPane.showInputDialog(this, "Enter the room number to vacate:");

        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room number cannot be empty.");
            return;
        }

        String checkQuery = "SELECT room_id, status FROM rooms WHERE room_number = ?";
        String updateRoomQuery = "UPDATE rooms SET status = 'Available' WHERE room_number = ?";
        String updateBookingQuery = "UPDATE booking_requests SET status = 'Vacated' WHERE room_id = ? AND status = 'Approved'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement updateRoomStmt = conn.prepareStatement(updateRoomQuery);
             PreparedStatement updateBookingStmt = conn.prepareStatement(updateBookingQuery)) {

            checkStmt.setString(1, roomNumber.trim());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                int roomId = rs.getInt("room_id");

                if ("Available".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "Room is already available.");
                } else {
                    // Step 1: Update room status to 'Available'
                    updateRoomStmt.setString(1, roomNumber.trim());
                    updateRoomStmt.executeUpdate();

                    // Step 2: Update associated approved booking request to 'Vacated'
                    updateBookingStmt.setInt(1, roomId);
                    updateBookingStmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Room vacated successfully.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Room not found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error vacating room.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ManageRoomsPage::new);
    }
}

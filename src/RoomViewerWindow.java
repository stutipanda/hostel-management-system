import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RoomViewerWindow extends JFrame {
    private JTable roomTable;
    private DefaultTableModel roomModel;

    public RoomViewerWindow() {
        setTitle("All Rooms - Hostel Management");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        roomModel = new DefaultTableModel(new String[]{"Room ID", "Room Number", "Status"}, 0);
        roomTable = new JTable(roomModel);

        fetchAllRooms();

        add(new JScrollPane(roomTable), BorderLayout.CENTER);
        setVisible(true);
    }

    private void fetchAllRooms() {
        String query = """
            SELECT 
                r.room_id, 
                r.room_number,
                CASE
                    WHEN EXISTS (
                        SELECT 1 FROM booking_requests br 
                        WHERE br.room_id = r.room_id AND br.status = 'Approved'
                    )
                    THEN 'Occupied'
                    ELSE 'Available'
                END AS status
            FROM rooms r
            ORDER BY r.room_id
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            roomModel.setRowCount(0); // Clear previous data

            while (rs.next()) {
                roomModel.addRow(new Object[]{
                        rs.getInt("room_id"),
                        rs.getString("room_number"),
                        rs.getString("status") // Use directly from query
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching room data!");
        }
    }
}

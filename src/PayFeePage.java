import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PayFeePage extends JFrame {
    private String username;
    private JLabel feeLabel;

    public PayFeePage(String username) {
        this.username = username;
        setTitle("Pay Fee");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        feeLabel = new JLabel("Loading pending fee...", SwingConstants.CENTER);
        feeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton payButton = new JButton("Pay Now");
        payButton.addActionListener(e -> payFee());

        add(feeLabel, BorderLayout.CENTER);
        add(payButton, BorderLayout.SOUTH);

        fetchPendingFee();
        setVisible(true);
    }

    private void fetchPendingFee() {
        String query = "SELECT f.pending_amount FROM fees f JOIN users u ON f.student_id = u.id WHERE u.username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double pending = rs.getDouble("pending_amount");
                feeLabel.setText("Pending Amount: ₹" + pending);
            } else {
                feeLabel.setText("No pending fees.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching fee data.");
        }
    }

    private void payFee() {
        String update = "UPDATE fees SET pending_amount = 0 WHERE student_id = (SELECT id FROM users WHERE username = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(update)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Fee paid successfully!");
            feeLabel.setText("No pending fees.");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Payment failed.");
        }
    }
}


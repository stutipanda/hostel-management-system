import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER, 30, 50));

        JButton manageRoomsButton = new JButton("Manage Rooms");
        JButton viewFeesButton = new JButton("View Fees");
        JButton handleComplaintsButton = new JButton("Handle Complaints");
        JButton logoutButton = new JButton("Logout");

        // 🛠 Close AdminDashboard and open ManageRoomsPage
        manageRoomsButton.addActionListener(e -> {
            dispose();
            new ManageRoomsPage();
        });

        // ✅ Open ManageFeesPage instead of message
        viewFeesButton.addActionListener(e -> {
            dispose();
            new ManageFeesPage(); // Fee management page for admin
        });

        // ✅ Open HandleComplaintsPage
        handleComplaintsButton.addActionListener(e -> {
            dispose();
            new HandleComplaintsPage(); // Open complaint handler window
        });

        // 🧵 Bold text only for Logout button (no color)
        logoutButton.setFont(logoutButton.getFont().deriveFont(Font.BOLD));
        logoutButton.addActionListener(e -> {
            dispose(); // Close AdminDashboard
            SwingUtilities.invokeLater(LoginForm::new); // Open LoginForm safely
        });

        add(manageRoomsButton);
        add(viewFeesButton);
        add(handleComplaintsButton);
        add(logoutButton);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new); // Safe launch
    }
}

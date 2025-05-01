import javax.swing.*;
import java.awt.*;

public class StudentDashboard extends JFrame {
    private String username; // Logged-in student

    public StudentDashboard(String username) {
        this.username = username;

        setTitle("Student Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create panel to hold buttons
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(150, 300, 50, 300)); // Padding

        // Buttons
        JButton bookRoomButton = new JButton("Book Room");
        JButton payFeeButton = new JButton("Pay Fee");
        JButton lodgeComplaintButton = new JButton("Lodge Complaint");
        JButton logoutButton = new JButton("Logout");

        // Set button sizes
        Dimension buttonSize = new Dimension(200, 40);
        bookRoomButton.setMaximumSize(buttonSize);
        payFeeButton.setMaximumSize(buttonSize);
        lodgeComplaintButton.setMaximumSize(buttonSize);
        logoutButton.setMaximumSize(buttonSize);

        // 🧵 Bold text for Logout button only
        logoutButton.setFont(logoutButton.getFont().deriveFont(Font.BOLD));

        // Action listeners
        bookRoomButton.addActionListener(e -> new BookRoomPage(username));
        payFeeButton.addActionListener(e -> new PayFeePage(username));
        lodgeComplaintButton.addActionListener(e -> new LodgeComplaintPage(username));
        logoutButton.addActionListener(e -> {
            dispose(); // Close Student Dashboard
            EventQueue.invokeLater(() -> {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true); // Show login form after logout
                dispose();
            });
        });

        // Add buttons with spacing
        mainPanel.add(bookRoomButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(payFeeButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(lodgeComplaintButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(logoutButton);

        // Add to frame
        add(mainPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentDashboard("demoUser")); // Launch properly
    }
}

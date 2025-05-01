import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeBox;

    public LoginForm() {
        setTitle("Hostel Management System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        JLabel userLabel = new JLabel("Username:", SwingConstants.CENTER);
        usernameField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:", SwingConstants.CENTER);
        passwordField = new JPasswordField();

        JLabel typeLabel = new JLabel("Login As:", SwingConstants.CENTER);
        userTypeBox = new JComboBox<>(new String[]{"Admin", "Student"});

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener(e -> authenticateUser());
        registerButton.addActionListener(e -> new RegisterForm()); // Open registration form

        add(userLabel);
        add(usernameField);
        add(passLabel);
        add(passwordField);
        add(typeLabel);
        add(userTypeBox);
        add(registerButton);
        add(loginButton);

        setVisible(true);
    }

    private void authenticateUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String userType = (String) userTypeBox.getSelectedItem();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT username, role FROM users WHERE username=? AND password=? AND role=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, userType);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("✅ User Found: " + rs.getString("username") + " | Role: " + rs.getString("role"));
                JOptionPane.showMessageDialog(this, "Login Successful!");

                if ("Student".equals(userType)) {
                    new StudentDashboard(username).setVisible(true);
                } else {
                    new AdminDashboard().setVisible(true);
                }

                dispose(); // Close Login Window
            } else {
                System.out.println("❌ User not found in database!");
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new LoginForm();
    }
}

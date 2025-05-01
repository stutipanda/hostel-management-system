import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

 class RegisterForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public RegisterForm() {
        setTitle("Register New User");
        setSize(400, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JLabel roleLabel = new JLabel("Register As:");
        roleBox = new JComboBox<>(new String[]{"Admin", "Student"});

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> registerUser());

        add(userLabel);
        add(usernameField);
        add(passLabel);
        add(passwordField);
        add(roleLabel);
        add(roleBox);
        add(new JLabel());
        add(registerButton);

        setVisible(true);
    }

     private void registerUser() {
         String username = usernameField.getText();
         String password = new String(passwordField.getPassword());
         String role = (String) roleBox.getSelectedItem();

         try (Connection conn = DBConnection.getConnection()) {
             String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
             PreparedStatement stmt = conn.prepareStatement(query);
             stmt.setString(1, username);
             stmt.setString(2, password);
             stmt.setString(3, role);

             int rowsInserted = stmt.executeUpdate();
             if (rowsInserted > 0) {
                 JOptionPane.showMessageDialog(this, "User registered successfully!");
                 System.out.println("User " + username + " added to database.");
             } else {
                 JOptionPane.showMessageDialog(this, "Registration failed!");
                 System.out.println("Registration failed, no rows inserted.");
             }

             dispose();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 }

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Signup extends JFrame implements ActionListener {
    private static final String PGHOST = "ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "knjiznica";
    private static final String PGUSER = "knjiznica_owner";
    private static final String PGPASSWORD = "unjLsCbBK4r3";

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton signupButton;

    public Signup() {
        setTitle("Signup");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Zapre samo Signup okno
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        signupButton = new JButton("Signup");
        signupButton.addActionListener(this);
        add(signupButton);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == signupButton) {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            try {
                Connection connection = DriverManager.getConnection("jdbc:postgresql://" + PGHOST + "/" + PGDATABASE,
                        PGUSER, PGPASSWORD);

                if (signup(connection, username, email, password)) {
                    JOptionPane.showMessageDialog(this, "Registration successful!");
                    dispose(); // Zapre Signup okno po uspešni registraciji
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed. Please try again.");
                }

                connection.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error connecting to the database: " + ex.getMessage());
            }
        }
    }

    private static boolean signup(Connection connection, String username, String email, String password) throws SQLException {
        String sql = "INSERT INTO uporabniki (username, email, geslo) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public static void main(String[] args) {
        new Signup();
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main extends JFrame implements ActionListener {
    private static final String PGHOST = "ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "knjiznica";
    private static final String PGUSER = "knjiznica_owner";
    private static final String PGPASSWORD = "unjLsCbBK4r3";

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private int userId; // To shranjuje ID uporabnika

    public Main() {
        setTitle("Prijava");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Geslo:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Prijava");
        loginButton.addActionListener(this);
        add(loginButton);

        signupButton = new JButton("Signup");
        signupButton.addActionListener(this);
        add(signupButton);
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.showLoginWindow();
    }

    private void showLoginWindow() {
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            try {
                Connection connection = DriverManager.getConnection("jdbc:postgresql://" + PGHOST + "/" + PGDATABASE,
                        PGUSER, PGPASSWORD);

                if (login(connection, email, password)) {
                    JOptionPane.showMessageDialog(this, "Uspešno ste se prijavili!");

                    // Shranimo ID uporabnika
                    userId = getUserId(connection, email, password);

                    new domacastran(userId); // Preko konstruktora prenesemo ID uporabnika
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Napačen email ali geslo.");
                }

                connection.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Napaka pri povezavi z bazo: " + ex.getMessage());
            }
        } else if (e.getSource() == signupButton) {
            new Signup();
        }
    }

    private static boolean login(Connection connection, String email, String password) throws SQLException {
        String sql = "SELECT * FROM uporabniki WHERE email = ? AND geslo = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    // Metoda za pridobitev ID-ja uporabnika
    private static int getUserId(Connection connection, String email, String password) throws SQLException {
        String sql = "SELECT id FROM uporabniki WHERE email = ? AND geslo = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }
        return -1; // Če uporabnik ni najden, vrnemo -1
    }
}

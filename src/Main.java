import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Login page
public class Main extends JFrame implements ActionListener {
    // Povezava z bazo podatkov
    private static final String PGHOST = "ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "knjiznica";
    private static final String PGUSER = "knjiznica_owner";
    private static final String PGPASSWORD = "unjLsCbBK4r3";

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton; // Dodamo gumb za prijavo

    public Main() {
        // Nastavi okno
        setTitle("Prijava");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2)); // Spremenimo na 4 vrstice, da lahko dodamo gumb Signup

        // Dodaj komponente
        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Geslo:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Prijava");
        loginButton.addActionListener(this);
        add(loginButton);

        signupButton = new JButton("Signup"); // Dodamo gumb Signup
        signupButton.addActionListener(this);
        add(signupButton);
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.showLoginWindow(); // Prikaži okno za prijavo
    }

    // Metoda za prikaz okna za prijavo
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

                    // Če je prijava uspešna, odprite novo okno domače strani
                    new domacastran();

                    // Zaprite okno za prijavo
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Napačen email ali geslo.");
                }

                connection.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Napaka pri povezavi z bazo: " + ex.getMessage());
            }
        } else if (e.getSource() == signupButton) {
            // Če je kliknjen gumb Signup, odpremo novo okno za prijavo
            new Signup();
        }
    }

    // Metoda za preverjanje emaila in gesla uporabnika
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
}

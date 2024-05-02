import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DodajZanr extends JFrame implements ActionListener {
    private JTextField imeField, opisField;
    private JButton dodajButton;
    private DodajKnjigo parentFrame; // Dodali smo referenco na starševsko okno

    public DodajZanr(DodajKnjigo parentFrame) {
        this.parentFrame = parentFrame; // Pravilno nastavimo starševsko okno
        setTitle("Dodajanje Novega Žanra");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("Ime:"));
        imeField = new JTextField();
        add(imeField);

        add(new JLabel("Opis:"));
        opisField = new JTextField();
        add(opisField);

        dodajButton = new JButton("Dodaj Žanr");
        dodajButton.addActionListener(this);
        add(dodajButton);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == dodajButton) {
            String ime = imeField.getText();
            String opis = opisField.getText();

            // Dodajanje žanra v bazo podatkov
            try {
                Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                        "knjiznica_owner", "unjLsCbBK4r3");

                String sql = "INSERT INTO zanri (ime, opis) VALUES (?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, ime);
                statement.setString(2, opis);
                statement.executeUpdate();

                JOptionPane.showMessageDialog(this, "Žanr uspešno dodan!");

                connection.close();
                parentFrame.refreshComboBox();

                // Zapremo to okno
                this.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Napaka pri dodajanju žanra: " + ex.getMessage());
            }
        }
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;
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

                // Klic shranjenega podprograma za dodajanje žanra
                String sql = "{ ? = CALL dodaj_zanr(?, ?) }";
                try (CallableStatement statement = connection.prepareCall(sql)) {
                    statement.registerOutParameter(1, Types.BOOLEAN);
                    statement.setString(2, ime);
                    statement.setString(3, opis);
                    statement.execute();
                    boolean insertionSuccessful = statement.getBoolean(1);

                    if (insertionSuccessful) {
                        JOptionPane.showMessageDialog(this, "Žanr uspešno dodan!");
                        parentFrame.refreshComboBox(); // Osvežitev JComboBox v starševskem oknu
                        this.dispose(); // Zaprtje trenutnega okna
                    } else {
                        JOptionPane.showMessageDialog(this, "Napaka pri dodajanju žanra.");
                    }
                }

                connection.close();
                parentFrame.refreshComboBox();
                this.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Napaka pri dodajanju žanra: " + ex.getMessage());
            }
        }
    }

}

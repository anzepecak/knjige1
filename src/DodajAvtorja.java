import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Calendar;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;


public class DodajAvtorja extends JFrame implements ActionListener {
    private JTextField imeField, priimekField, nagradeField;
    private JComboBox<String> drzavaComboBox;
    private JDatePicker datumPicker;
    private JButton dodajButton;
    private DodajKnjigo parentFrame; // Dodali smo referenco na starševsko okno
    private int userId;
    public DodajAvtorja(DodajKnjigo parentFrame) {
        this.parentFrame = parentFrame; // Pravilno nastavimo referenco na starševsko okno
        setTitle("Dodajanje Novega Avtorja");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2));
        this.userId = userId;
        add(new JLabel("Ime:"));
        imeField = new JTextField();
        add(imeField);

        add(new JLabel("Priimek:"));
        priimekField = new JTextField();
        add(priimekField);

        add(new JLabel("Država:"));
        drzavaComboBox = new JComboBox<>();
        populateDrzavaComboBox();
        add(drzavaComboBox);

        add(new JLabel("Datum rojstva:"));
        datumPicker = createDatePicker();
        add((Component) datumPicker);

        add(new JLabel("Nagrade:"));
        nagradeField = new JTextField();
        add(nagradeField);

        dodajButton = new JButton("Dodaj Avtorja");
        dodajButton.addActionListener(this);
        add(dodajButton);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == dodajButton) {
            String ime = imeField.getText();
            String priimek = priimekField.getText();
            String drzava = (String) drzavaComboBox.getSelectedItem();
            Date datum = (Date) datumPicker.getModel().getValue();
            String nagrade = nagradeField.getText();

            // Dodajanje avtorja v bazo podatkov
            try {
                Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                        "knjiznica_owner", "unjLsCbBK4r3");

                // Pridobimo ID države glede na izbrano ime države
                int drzavaId = getDrzavaId(drzava);

                String sql = "INSERT INTO avtorji (ime, priimek, drzava_id, dat_roj, nagrade) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, ime);
                statement.setString(2, priimek);
                statement.setInt(3, drzavaId);
                statement.setDate(4, new java.sql.Date(datum.getTime()));
                statement.setString(5, nagrade);
                statement.executeUpdate();

                JOptionPane.showMessageDialog(this, "Avtor uspešno dodan!");

                connection.close();

                // Osvežimo JComboBox v starševskem oknu
                parentFrame.refreshComboBox();

                // Zapremo to okno
                this.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Napaka pri dodajanju avtorja: " + ex.getMessage());
            }
        }
    }

    private void populateDrzavaComboBox() {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                "knjiznica_owner", "unjLsCbBK4r3")) {
            String sql = "SELECT ime FROM drzave";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String imeDrzave = resultSet.getString("ime");
                drzavaComboBox.addItem(imeDrzave);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private int getDrzavaId(String drzava) {
        int drzavaId = 0;
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                "knjiznica_owner", "unjLsCbBK4r3")) {
            String sql = "SELECT id FROM drzave WHERE ime = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, drzava);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                drzavaId = resultSet.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return drzavaId;
    }

    private JDatePicker createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        return datePicker;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DodajKnjigo dodajKnjigo = new DodajKnjigo(1); // Predpostavljeno ID
            new DodajAvtorja(dodajKnjigo);
        });
    }

}

class DateLabelFormatter2 extends JFormattedTextField.AbstractFormatter {

    private String datePattern = "yyyy-MM-dd";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

    @Override
    public Object stringToValue(String text) {
        try {
            return dateFormatter.parseObject(text);
        } catch (java.text.ParseException e) {
            return null;
        }
    }

    @Override
    public String valueToString(Object value) {
        if (value != null) {
            Calendar cal = (Calendar) value;
            return dateFormatter.format(cal.getTime());
        }

        return "";
    }
}

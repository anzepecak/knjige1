import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Calendar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class DodajKnjigo extends JFrame implements ActionListener {
    private JTextField naslovField;
    private JComboBox<String> avtorComboBox, zanrComboBox;
    private JDatePicker datumPicker;
    private JButton dodajButton;
    private JButton dodajAvtorjaButton;
    private JButton dodajZanrButton;
    private Map<String, Integer> avtorjiMap = new HashMap<>();
    private Map<String, Integer> zanriMap = new HashMap<>();
    private int userId;
    public DodajKnjigo(int userId) {
        this.userId = userId; // Shranimo ID uporabnika
        setTitle("Dodajanje Nove Knjige");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        // Using GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add some padding

        gbc.gridx = 0;
        gbc.gridy = 0;

        add(new JLabel("Naslov:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3; // Expanded width
        naslovField = new JTextField(15); // Increased columns
        add(naslovField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("Avtor:"), gbc);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                dispose(); // Zapremo okno DodajKnjigo
                refreshTable(); // Osvežimo tabelo na domacastran
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 1;

        avtorComboBox = new JComboBox<>();
        populateAvtorjiComboBox();
        add(avtorComboBox, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        dodajAvtorjaButton = new JButton("Dodaj Avtorja");
        dodajAvtorjaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DodajAvtorja dodajAvtorja = new DodajAvtorja(DodajKnjigo.this); // Shranite referenco na okno DodajKnjigo
            }
        });

        add(dodajAvtorjaButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Žanr:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        zanrComboBox = new JComboBox<>();
        populateZanriComboBox();
        add(zanrComboBox, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        dodajZanrButton = new JButton("Dodaj Žanr");
        dodajZanrButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new DodajZanr(DodajKnjigo.this); // Pravilno nastavimo starševsko okno
            }
        });
        add(dodajZanrButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Datum Izdaje:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3; // Expanded width
        datumPicker = createDatePicker();
        add((Component) datumPicker, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        dodajButton = new JButton("Dodaj Knjigo");
        dodajButton.addActionListener(this);
        add(dodajButton, gbc);

        pack(); // Adjust window size to fit components
        setVisible(true);
    }
    public void refreshTable() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof domacastran) {
                ((domacastran) window).refresh();
            }
        }
    }
    public void refreshComboBox() {
        // Počistimo obstoječe vrednosti v JComboBox
        avtorComboBox.removeAllItems();

        // Ponovno napolnimo JComboBox z avtorji
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                "knjiznica_owner", "unjLsCbBK4r3")) {
            String sql = "SELECT ime, priimek FROM avtorji";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String ime = resultSet.getString("ime");
                String priimek = resultSet.getString("priimek");
                String avtor = ime + " " + priimek;
                avtorComboBox.addItem(avtor);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private void populateAvtorjiComboBox() {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                "knjiznica_owner", "unjLsCbBK4r3")) {
            String sql = "SELECT id, ime, priimek FROM avtorji";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String ime = resultSet.getString("ime");
                String priimek = resultSet.getString("priimek");
                String imePriimek = ime + " " + priimek;
                avtorjiMap.put(imePriimek, id);
                avtorComboBox.addItem(imePriimek);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void populateZanriComboBox() {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                "knjiznica_owner", "unjLsCbBK4r3")) {
            String sql = "SELECT id, ime FROM zanri";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String ime = resultSet.getString("ime");
                zanriMap.put(ime, id);
                zanrComboBox.addItem(ime);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JDatePicker createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePicker datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        return datePicker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == dodajButton) {
            String naslov = naslovField.getText();
            String avtor = (String) avtorComboBox.getSelectedItem();
            String zanr = (String) zanrComboBox.getSelectedItem();
            Date datum = (Date) datumPicker.getModel().getValue();

            if (naslov.isEmpty() || avtor == null || zanr == null || datum == null) {
                JOptionPane.showMessageDialog(this, "Prosimo, izpolnite vsa polja.");
                return;
            }

            try {
                Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                        "knjiznica_owner", "unjLsCbBK4r3");

                int avtorId = avtorjiMap.get(avtor);
                int zanrId = zanriMap.get(zanr);

                // Call the server-side function to add the book
                String sql = "{ ? = CALL dodaj_knjigo(?, ?, ?, ?, ?) }";
                try (CallableStatement statement = connection.prepareCall(sql)) {
                    statement.registerOutParameter(1, Types.BOOLEAN);
                    statement.setString(2, naslov);
                    statement.setInt(3, avtorId);
                    statement.setInt(4, zanrId);
                    statement.setDate(5, new java.sql.Date(datum.getTime()));
                    statement.setInt(6, getUserId());
                    statement.execute();
                    boolean insertionSuccessful = statement.getBoolean(1);

                    if (insertionSuccessful) {
                        JOptionPane.showMessageDialog(this, "Knjiga uspešno dodana!");
                        refreshTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "Napaka pri dodajanju knjige.");
                    }
                }

                connection.close();
                dispose(); // Close the DodajKnjigo window
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Napaka pri dodajanju knjige: " + ex.getMessage());
            }
        }
    }



    private int getUserId() {
        return userId;
    }

    public static void main(String[] args) {
        // Zaženemo aplikacijo z ID-jem uporabnika
        int userId = 1; // Predpostavimo, da imamo ID prijavljenega uporabnika
        SwingUtilities.invokeLater(() -> {
            new DodajKnjigo(userId);
        });
    }

}

class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {

    private String datePattern = "yyyy-MM-dd";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

    @Override
    public Object stringToValue(String text) {
        try {
            return dateFormatter.parseObject(text);
        } catch (ParseException e) {
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

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
import java.text.ParseException;
import java.sql.*;
public class UpdateKnjiga extends JFrame {

    private int userId;
    private String bookTitle;
    private domacastran parentFrame;

    private JTextField naslovField;
    private JTextField avtorImeField;
    private JTextField avtorPriimekField; // Dodano polje za priimek avtorja
    private JTextField zanrField;
    private JTextField datIzdajeField;

    private String oldAuthorName = "";
    private String oldAuthorSurname = "";
    private String oldGenre = "";
    public UpdateKnjiga(int userId, String bookTitle, domacastran parentFrame) {

        this.userId = userId;
        this.bookTitle = bookTitle;
        this.parentFrame = parentFrame;

        setTitle("Posodobi Knjigo");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2));
        add(panel, BorderLayout.CENTER);

        JLabel naslovLabel = new JLabel("Naslov:");
        naslovField = new JTextField();
        panel.add(naslovLabel);
        panel.add(naslovField);

        JLabel avtorImeLabel = new JLabel("Ime avtorja:");
        avtorImeField = new JTextField();
        panel.add(avtorImeLabel);
        panel.add(avtorImeField);

        JLabel avtorPriimekLabel = new JLabel("Priimek avtorja:"); // Dodan label za priimek avtorja
        avtorPriimekField = new JTextField();
        panel.add(avtorPriimekLabel);
        panel.add(avtorPriimekField);

        JLabel zanrLabel = new JLabel("Žanr:");
        zanrField = new JTextField();
        panel.add(zanrLabel);
        panel.add(zanrField);

        JLabel datIzdajeLabel = new JLabel("Datum Izdaje:");
        datIzdajeField = new JTextField();
        panel.add(datIzdajeLabel);
        panel.add(datIzdajeField);

        JButton updateButton = new JButton("Posodobi");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateBook();
            }
        });
        add(updateButton, BorderLayout.SOUTH);

        // Pridobimo podatke o knjigi iz baze in jih prikažemo v poljih za urejanje
        displayBookInfo();

        setVisible(true);
    }

    private void displayBookInfo() {

        try {
            // Ustvarimo povezavo z bazo podatkov
            Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                    "knjiznica_owner", "unjLsCbBK4r3");

            // Izvedemo poizvedbo za pridobitev podatkov o knjigi
            String sql = "SELECT naslov, avtorji.ime AS ime_avtorja, avtorji.priimek AS priimek_avtorja, zanri.ime AS zanr, dat_izdaje " +
                    "FROM knjige " +
                    "INNER JOIN avtorji ON knjige.avtor_id = avtorji.id " +
                    "INNER JOIN zanri ON knjige.zanr_id = zanri.id " +
                    "WHERE naslov = ? AND uporabnik_id = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, bookTitle);
            statement.setInt(2, userId);
            ResultSet resultSet = statement.executeQuery();

            // Če najdemo knjigo, jo prikažemo v poljih za urejanje
            if (resultSet.next()) {
                naslovField.setText(resultSet.getString("naslov"));
                avtorImeField.setText(resultSet.getString("ime_avtorja")); // Posodobljeno polje za ime avtorja
                avtorPriimekField.setText(resultSet.getString("priimek_avtorja")); // Posodobljeno polje za priimek avtorja
                zanrField.setText(resultSet.getString("zanr"));
                datIzdajeField.setText(resultSet.getString("dat_izdaje"));

                oldAuthorName = resultSet.getString("ime_avtorja");
                oldAuthorSurname = resultSet.getString("priimek_avtorja");
                oldGenre = resultSet.getString("zanr");
            }



            // Zapremo povezavo z bazo podatkov
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Napaka pri pridobivanju informacij o knjigi: " + ex.getMessage());
        }
    }


    private void updateBook() {
        try {
            // Ustvarimo povezavo z bazo podatkov
            Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                    "knjiznica_owner", "unjLsCbBK4r3");

            String newTitle = naslovField.getText();
            String newAuthorName = avtorImeField.getText();
            String newAuthorSurname = avtorPriimekField.getText();
            String newGenre = zanrField.getText();
            String newPublicationDateStr = datIzdajeField.getText(); // Niz znakov za datum izdaje

            // Pretvorimo niz znakov v tip Date
            java.sql.Date newPublicationDate = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = sdf.parse(newPublicationDateStr);
                newPublicationDate = new java.sql.Date(utilDate.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Napaka pri oblikovanju datuma: " + e.getMessage());
                return; // Prekinemo posodabljanje, če pride do napake pri pretvorbi datuma
            }

            // Posodobi naslov knjige
            String updateTitleSql = "{ ? = CALL posodobi_naslov_knjige(?, ?, ?) }";
            try (CallableStatement updateTitleStatement = connection.prepareCall(updateTitleSql)) {
                updateTitleStatement.registerOutParameter(1, Types.BOOLEAN);
                updateTitleStatement.setString(2, newTitle);
                updateTitleStatement.setString(3, bookTitle);
                updateTitleStatement.setInt(4, userId);
                updateTitleStatement.execute();
                boolean titleUpdateSuccessful = updateTitleStatement.getBoolean(1);
            }

            // Posodobi ime avtorja, če se je spremenilo
            if (!newAuthorName.equals(oldAuthorName)) {
                String updateAuthorNameSql = "{ ? = CALL posodobi_ime_avtorja(?, ?, ?) }";
                try (CallableStatement updateAuthorNameStatement = connection.prepareCall(updateAuthorNameSql)) {
                    updateAuthorNameStatement.registerOutParameter(1, Types.BOOLEAN);
                    updateAuthorNameStatement.setString(2, newAuthorName);
                    updateAuthorNameStatement.setString(3, oldAuthorName);
                    updateAuthorNameStatement.setString(4, oldAuthorSurname);
                    updateAuthorNameStatement.execute();
                    boolean authorNameUpdateSuccessful = updateAuthorNameStatement.getBoolean(1);
                }
            }

            // Posodobi priimek avtorja, če se je spremenilo
            if (!newAuthorSurname.equals(oldAuthorSurname)) {
                String updateAuthorSurnameSql = "{ ? = CALL posodobi_priimek_avtorja(?, ?, ?) }";
                try (CallableStatement updateAuthorSurnameStatement = connection.prepareCall(updateAuthorSurnameSql)) {
                    updateAuthorSurnameStatement.registerOutParameter(1, Types.BOOLEAN);
                    updateAuthorSurnameStatement.setString(2, newAuthorSurname);
                    updateAuthorSurnameStatement.setString(3, newAuthorName);
                    updateAuthorSurnameStatement.setString(4, oldAuthorSurname);
                    updateAuthorSurnameStatement.execute();
                    boolean authorSurnameUpdateSuccessful = updateAuthorSurnameStatement.getBoolean(1);
                }
            }

            // Posodobi žanr knjige
            String updateGenreSql = "{ ? = CALL posodobi_zanr_knjige(?, ?) }";
            try (CallableStatement updateGenreStatement = connection.prepareCall(updateGenreSql)) {
                updateGenreStatement.registerOutParameter(1, Types.BOOLEAN);
                updateGenreStatement.setString(2, newGenre);
                updateGenreStatement.setString(3, oldGenre);
                updateGenreStatement.execute();
                boolean genreUpdateSuccessful = updateGenreStatement.getBoolean(1);
            }

            // Posodobi datum izdaje knjige
            String updatePublicationDateSql = "{ ? = CALL posodobi_datum_izdaje_knjige(?, ?, ?) }";
            try (CallableStatement updatePublicationDateStatement = connection.prepareCall(updatePublicationDateSql)) {
                updatePublicationDateStatement.registerOutParameter(1, Types.BOOLEAN);
                updatePublicationDateStatement.setDate(2, newPublicationDate);
                updatePublicationDateStatement.setString(3, newTitle);
                updatePublicationDateStatement.setInt(4, userId);
                updatePublicationDateStatement.execute();
                boolean publicationDateUpdateSuccessful = updatePublicationDateStatement.getBoolean(1);
            }

            // Če je bila knjiga uspešno posodobljena, osvežimo tabelo in zapremo okno
            parentFrame.refresh(); // Osvežimo tabelo na glavnem oknu
            JOptionPane.showMessageDialog(this, "Knjiga je bila uspešno posodobljena.");
            dispose(); // Zapremo okno za posodabljanje knjige

            // Zapremo povezavo z bazo podatkov
            connection.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Napaka pri posodabljanju knjige: " + ex.getMessage());
        }
    }





}

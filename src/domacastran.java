import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;
public class domacastran extends JFrame {
    private JTable table;
    private int userId; // Dodan atribut za shranjevanje ID-ja uporabnika

    // Posodobljen konstruktor, ki sprejme ID uporabnika kot parameter
    public domacastran(int userId) {
        this.userId = userId; // Shranimo ID uporabnika
        setTitle("Domača Stran");
        setSize(800, 400); // Spremenjena velikost za prostor za gumbe
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columns = {"Naslov", "Ime avtorja", "Priimek avtorja", "Žanr", "Datum Izdaje", "Delete", "Update"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        // Dodajanje gumbov v stolpce "Delete" in "Update"
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(5).setCellRenderer(new ButtonRenderer());
        columnModel.getColumn(5).setCellEditor(new ButtonEditorDelete(new JCheckBox()));
        columnModel.getColumn(6).setCellRenderer(new ButtonRenderer());
        columnModel.getColumn(6).setCellEditor(new ButtonEditorUpdate(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Dodajanje gumba za dodajanje knjige
        JButton addButton = new JButton("Dodaj");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Koda za odpiranje okna za dodajanje knjige
                new DodajKnjigo(userId);

            }
        });
        add(addButton, BorderLayout.SOUTH);

        // Load user's books
        loadUserBooks();

        setVisible(true);
    }

    private void loadUserBooks() {
        try {
            // Establish connection to the database
            Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                    "knjiznica_owner", "unjLsCbBK4r3");

            // Fetch user's books from the database
            String sql = "SELECT knjige.naslov, avtorji.ime, avtorji.priimek, zanri.ime AS zanr, knjige.dat_izdaje " +
                    "FROM knjige " +
                    "INNER JOIN avtorji ON knjige.avtor_id = avtorji.id " +
                    "INNER JOIN zanri ON knjige.zanr_id = zanri.id " +
                    "WHERE knjige.uporabnik_id = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId); // Uporabimo ID uporabnika, ki smo ga prejeli preko konstruktora
            ResultSet resultSet = statement.executeQuery();

            // Populate the table with fetched data
            while (resultSet.next()) {
                String title = resultSet.getString("naslov");
                String authorName = resultSet.getString("ime");
                String authorSurname = resultSet.getString("priimek");
                String genre = resultSet.getString("zanr");
                String publicationDate = resultSet.getString("dat_izdaje");
                String[] row = {title, authorName, authorSurname, genre, publicationDate, "Delete", "Update"};
                ((DefaultTableModel) table.getModel()).addRow(row);
            }

            // Close the database connection
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + ex.getMessage());
        }
    }

    // Metoda za brisanje knjige
    private void deleteBook(int rowIndex) {
        try {
            // Establish connection to the database
            Connection connection = DriverManager.getConnection("jdbc:postgresql://ep-billowing-feather-a2yuhppe.eu-central-1.aws.neon.tech/knjiznica",
                    "knjiznica_owner", "unjLsCbBK4r3");

            // Get book title from selected row
            String bookTitle = (String) table.getValueAt(rowIndex, 0);

            // Delete book using the server-side function
            String sql = "{ ? = CALL delete_book(?, ?) }";
            try (CallableStatement statement = connection.prepareCall(sql)) {
                statement.registerOutParameter(1, Types.BOOLEAN);
                statement.setString(2, bookTitle);
                statement.setInt(3, userId);
                statement.execute();
                boolean deletionSuccessful = statement.getBoolean(1);

                if (deletionSuccessful) {
                    // Remove the row from the table if deletion was successful
                    ((DefaultTableModel) table.getModel()).removeRow(rowIndex);
                } else {
                    JOptionPane.showMessageDialog(this, "The book does not belong to the current user.");
                }
            }

            // Close the database connection
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting book: " + ex.getMessage());
        }
    }


    public void refresh() {
        // Clear the table
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        // Reload user's books
        loadUserBooks();
    }


    // Metoda za odpiranje okna za posodabljanje knjige
    private void updateBook(int rowIndex) {
        try {
            // Pridobimo naslov knjige iz izbrane vrstice
            String bookTitle = (String) table.getValueAt(rowIndex, 0);

            // Ustvarimo novo okno za posodabljanje knjige
            new UpdateKnjiga(userId, bookTitle, this);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating book: " + ex.getMessage());
        }
    }

    // Razred ButtonRenderer za prikaz gumbov v tabeli
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            // Preverimo, ali je trenutni stolpec stolpec gumbov
            if (column == 7) { // Če je stolpec "Delete"
                setText("Delete");
                return this;
            } else if (column == 8) { // Če je stolpec "Update"
                setText("Update");
                return this;
            } else { // Če gre za drug stolpec
                setText((value == null) ? "" : value.toString());
                return this;
            }
        }
    }





    // Razred ButtonEditorDelete za urejanje celic z gumbom za brisanje
    class ButtonEditorDelete extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int rowIndex;

        public ButtonEditorDelete(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            rowIndex = row;
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                deleteBook(rowIndex); // Kličemo metodo za brisanje knjige
            }
            isPushed = false;
            return new String(label);
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    // Razred ButtonEditorUpdate za urejanje celic z gumbom za posodabljanje
    class ButtonEditorUpdate extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int rowIndex;

        public ButtonEditorUpdate(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            rowIndex = row;
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                updateBook(rowIndex); // Kličemo metodo za posodabljanje knjige
            }
            isPushed = false;
            return new String(label);
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    public static void main(String[] args) {
        // Zaženemo aplikacijo z ID-jem uporabnika
        SwingUtilities.invokeLater(() -> {
            new domacastran(1); // Tu lahko nastavite ID uporabnika, ki ga želite uporabiti
        });
    }
}

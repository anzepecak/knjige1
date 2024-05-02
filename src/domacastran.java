import javax.swing.*;

public class domacastran extends JFrame {
    public domacastran() {
        setTitle("Domača Stran");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Dodajte komponente za domačo stran
        JLabel label = new JLabel("Dobrodošli na domači strani!");
        add(label);

        setVisible(true);
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientWindow extends JDialog {
    private JPanel contentPane;
    private JButton dodajButton;
    private JButton usuńButton;
    private JTextField textField1;
    private JList<String> list1;
    private JButton zmieńIlośćButton;
    private DefaultListModel<String> listModel;

    private static final int SERVER_PORT = 8080;
    private static final String SERVER_ADDRESS = "localhost";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/listdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private StringBuilder stringBuilder = new StringBuilder();
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientWindow() {
        setContentPane(contentPane);
        setModal(true);
        setTitle("Client Window");

        listModel = new DefaultListModel<>();
        list1.setModel(listModel);
        list1.setFont(new Font("Poppins", Font.PLAIN, 20));

        dodajButton.addActionListener(this::onDodaj);
        usuńButton.addActionListener(this::onUsun);
        zmieńIlośćButton.addActionListener(this::onZmienIlosc);

        startConnectionChecker();
        loadDataFromDatabase();
        startServerConnection();
    }

    private void startServerConnection() {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    if (serverMessage.equals("update")) {
                        int response = showUpdateConfirmationDialog();
                        if (response == JOptionPane.YES_OPTION) {
                            loadDataFromDatabase();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int showUpdateConfirmationDialog() {
        String message = "Dane w bazie danych zostały zmienione. Czy chcesz je zaktualizować?";
        return JOptionPane.showConfirmDialog(this, message, "Aktualizacja danych", JOptionPane.YES_NO_OPTION);
    }

    private void onDodaj(ActionEvent e) {
        JTextField towarField = new JTextField(10);
        JTextField iloscJednostkaField = new JTextField(10);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Towar:"));
        inputPanel.add(towarField);
        inputPanel.add(new JLabel("Ilość:"));
        inputPanel.add(iloscJednostkaField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton dodaj = new JButton("Dodaj");
        JButton wyślij = new JButton("Wyślij");

        dodaj.addActionListener(ae -> {
            String towar = towarField.getText().trim();
            String iloscJednostka = iloscJednostkaField.getText().trim();

            if (iloscJednostka.matches(".*\\d+.*")) {
                stringBuilder.append(towar).append(" - ").append(iloscJednostka).append("\n");
                towarField.setText("");
                iloscJednostkaField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Nieprawidłowy format danych. Użyj formatu: ilość jednostkaMiary");
            }
        });

        wyślij.addActionListener(ae -> {
            String[] entries = stringBuilder.toString().split("\n");
            for (String entry : entries) {
                if (!entry.trim().isEmpty()) {
                    String[] parts = entry.split(" - ");
                    String towar = parts[0].trim();
                    String iloscJednostka = parts[1].trim();
                    String[] qtyAndUnit = iloscJednostka.split("(?<=\\d)(?=\\D)");

                    int ilosc = Integer.parseInt(qtyAndUnit[0].trim());
                    String jednostkaMiary = qtyAndUnit[1].trim();

                    try {
                        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                        InsertDataBase.insertData(connection, towar, ilosc, jednostkaMiary);
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            stringBuilder.setLength(0); // Clear the StringBuilder after sending
            loadDataFromDatabase();
            notifyServer();
        });

        buttonPanel.add(dodaj);
        buttonPanel.add(wyślij);

        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Dodaj", true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void onUsun(ActionEvent e) {
        int[] selectedIndices = list1.getSelectedIndices();
        if (selectedIndices.length > 0) {
            ArrayList<String> selectedValues = new ArrayList<>();
            for (int index : selectedIndices) {
                selectedValues.add(listModel.getElementAt(index));
            }

            try {
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                for (String value : selectedValues) {
                    String nazwa = value.split(" \\(")[0];
                    DeleteDataBase.deleteData(connection, nazwa);
                }
                connection.close();
                loadDataFromDatabase();
                notifyServer();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nie wybrano żadnej pozycji do usunięcia");
        }
    }

    private void onZmienIlosc(ActionEvent e) {
        int selectedIndex = list1.getSelectedIndex();
        if (selectedIndex != -1) {
            String selectedValue = listModel.getElementAt(selectedIndex);
            String nazwaProduktu = selectedValue.split(" \\(")[0];
            JTextField iloscField = new JTextField();
            int result = JOptionPane.showConfirmDialog(this, iloscField, "Podaj nową ilość", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    int nowaIlosc = Integer.parseInt(iloscField.getText().trim());
                    Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    InsertDataBase.updateQuantity(connection, nazwaProduktu, nowaIlosc);
                    connection.close();
                    loadDataFromDatabase();
                    notifyServer();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Podano nieprawidłową wartość dla ilości");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nie wybrano żadnego produktu do zmiany ilości");
        }
    }

    private void startConnectionChecker() {
        Thread connectionCheckerThread = new Thread(() -> {
            while (true) {
                try {
                    boolean isConnected = checkServerConnection();
                    SwingUtilities.invokeLater(() -> updateConnectionStatus(isConnected));
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        connectionCheckerThread.setDaemon(true);
        connectionCheckerThread.start();
    }

    private boolean checkServerConnection() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private void updateConnectionStatus(boolean isConnected) {
        if (isConnected) {
            textField1.setText("Online");
            textField1.setBackground(Color.GREEN);
        } else {
            textField1.setText("Offline");
            textField1.setBackground(Color.RED);
        }
    }

    private void loadDataFromDatabase() {
        new Thread(() -> {
            try {
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                ArrayList<String> items = SelectDataBase.retrieveData(connection);

                SwingUtilities.invokeLater(() -> {
                    listModel.clear();
                    for (String item : items) {
                        listModel.addElement(item);
                    }
                });

                connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void notifyServer() {
        if (out != null) {
            out.println("update");
        }
    }

    public static void main(String[] args) {
        ClientWindow dialog = new ClientWindow();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}

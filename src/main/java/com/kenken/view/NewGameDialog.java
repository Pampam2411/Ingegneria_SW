package com.kenken.view;

import com.kenken.generator.PuzzleGenerator; // Per le costanti di difficoltà

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class NewGameDialog extends JDialog {

    private JComboBox<Integer> sizeComboBox;
    private JComboBox<String> difficultyComboBox;
    private JButton startButton;
    private JButton cancelButton;

    private int selectedN = 0; // Valore di default o indicativo di nessuna selezione valida
    private String selectedDifficulty = null;
    private boolean confirmed = false;

    public NewGameDialog(Frame owner) {
        super(owner, "Configura Nuova Partita", true); // true per modale
        initComponents();
        layoutComponents();
        addListeners();

        pack(); // Dimensiona il dialogo in base ai suoi contenuti
        setResizable(false); // Impedisce il ridimensionamento
        setLocationRelativeTo(owner); // Centra rispetto alla finestra principale
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Comportamento alla chiusura
    }

    private void initComponents() {
        // Valori possibili per N (da 3 a 6 come specificato in Grid.java e PuzzleGenerator.java)
        Integer[] SIZES = {3, 4, 5, 6};
        sizeComboBox = new JComboBox<>(SIZES);
        sizeComboBox.setSelectedItem(4); // Valore predefinito N=4, puoi cambiarlo

        // Valori per la difficoltà (dalle costanti in PuzzleGenerator)
        // Assicurati che PuzzleGenerator.java sia compilato e accessibile
        String[] DIFFICULTIES = {
                PuzzleGenerator.DIFFICULTY_EASY,
                PuzzleGenerator.DIFFICULTY_MEDIUM,
                PuzzleGenerator.DIFFICULTY_HARD
        };
        difficultyComboBox = new JComboBox<>(DIFFICULTIES);
        difficultyComboBox.setSelectedItem(PuzzleGenerator.DIFFICULTY_EASY); // Valore predefinito

        startButton = new JButton("Avvia Partita");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(70, 130, 180)); // Steel Blue
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);


        cancelButton = new JButton("Annulla");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25)); // Più padding
        panel.setBackground(new Color(245, 245, 250)); // Sfondo lavanda chiaro
        GridBagConstraints gbc = new GridBagConstraints();

        // Stile per le etichette
        Font labelFont = new Font("SansSerif", Font.PLAIN, 14);

        // Etichetta e ComboBox per Dimensione
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(5, 5, 10, 10); // Aumentato bottom inset
        JLabel sizeLabel = new JLabel("Dimensione Griglia (N x N):");
        sizeLabel.setFont(labelFont);
        panel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fa espandere il combo box
        sizeComboBox.setFont(labelFont);
        panel.add(sizeComboBox, gbc);

        // Etichetta e ComboBox per Difficoltà
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(5, 5, 15, 10); // Aumentato bottom inset
        JLabel difficultyLabel = new JLabel("Livello Difficoltà:");
        difficultyLabel.setFont(labelFont);
        panel.add(difficultyLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        difficultyComboBox.setFont(labelFont);
        panel.add(difficultyComboBox, gbc);

        // Pannello per i pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // Aumentato gap tra pulsanti
        buttonPanel.setBackground(panel.getBackground()); // Stesso sfondo del pannello principale
        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Occupa entrambe le colonne
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // Non far espandere il buttonPanel
        gbc.insets = new Insets(20, 5, 5, 5); // Spazio sopra i pulsanti
        panel.add(buttonPanel, gbc);

        add(panel); // Aggiunge il pannello principale al JDialog
    }

    private void addListeners() {
        startButton.addActionListener((ActionEvent e) -> {
            // Objects.requireNonNull assicura che non ci siano NullPointerExceptions
            // anche se è improbabile con JComboBox pre-popolati.
            selectedN = (Integer) Objects.requireNonNull(sizeComboBox.getSelectedItem());
            selectedDifficulty = (String) Objects.requireNonNull(difficultyComboBox.getSelectedItem());
            confirmed = true;
            dispose(); // Chiude il dialogo
        });

        cancelButton.addActionListener((ActionEvent e) -> {
            confirmed = false;
            dispose(); // Chiude il dialogo
        });
    }

    // Metodi pubblici per ottenere i valori selezionati dalla classe chiamante (MainFrame)
    public int getSelectedN() {
        return selectedN;
    }

    public String getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    /*
    // Metodo main di esempio per testare il dialogo separatamente.
    // Commentalo o rimuovilo quando integri il dialogo nell'applicazione principale.
    public static void main(String[] args) {
        // Imposta un Look and Feel per il test, se desiderato
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // Per il test, passiamo null come owner. In un'app reale, sarà il MainFrame.
            NewGameDialog dialog = new NewGameDialog(null);
            dialog.setVisible(true); // Mostra il dialogo

            // Dopo che il dialogo è stato chiuso (dall'utente), controlla i risultati
            if (dialog.isConfirmed()) {
                System.out.println("Partita Confermata!");
                System.out.println("Dimensione N: " + dialog.getSelectedN());
                System.out.println("Difficoltà: " + dialog.getSelectedDifficulty());
            } else {
                System.out.println("Partita Annullata.");
            }
            // System.exit(0); // Esce dopo il test se eseguito standalone
        });
    }
    */
}

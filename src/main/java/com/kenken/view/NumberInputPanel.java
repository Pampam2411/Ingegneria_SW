package com.kenken.view;

import com.kenken.controller.GameController;
import com.kenken.model.GameModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NumberInputPanel extends JPanel {

    private GameController gameController;
    private GameModel gameModel; // Per sapere N e lo stato del gioco

    private JPanel buttonPanel; // Pannello interno per i pulsanti numerici
    private JButton clearButton;

    public NumberInputPanel(GameController controller, GameModel model) {
        this.gameController = controller;
        this.gameModel = model;

        setLayout(new BorderLayout(10, 5)); // Layout principale del pannello
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        // setBackground(new Color(210, 215, 220)); // Sfondo leggermente diverso

        buttonPanel = new JPanel(); // Verrà popolato in updateControls
        // Usiamo un FlowLayout per i pulsanti numerici, ma potremmo usare GridLayout
        // per una disposizione più ordinata se N è fisso o piccolo.
        // Per N variabile, FlowLayout è più semplice.
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
        add(buttonPanel, BorderLayout.CENTER);

        clearButton = new JButton("Canc"); // Testo breve per "Cancella"
        styleNumberButton(clearButton, true); // Stile speciale per il cancella
        clearButton.setToolTipText("Cancella il numero dalla cella selezionata (o premi 0)");
        clearButton.addActionListener((ActionEvent e) -> {
            if (gameController != null) {
                gameController.clearNumberViaButton();
            }
        });
        JPanel clearButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Allinea a destra
        clearButtonPanel.setOpaque(false);
        clearButtonPanel.add(clearButton);
        add(clearButtonPanel, BorderLayout.EAST); // Pulsante cancella a destra

        updateControls(); // Crea i pulsanti iniziali
    }

    private void styleNumberButton(JButton button, boolean isClearButton) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setMargin(new Insets(5, 8, 5, 8)); // Padding interno
        button.setFocusPainted(false);
        if (isClearButton) {
            button.setForeground(Color.RED.darker());
        }
    }

    /**
     * Aggiorna (o crea) i pulsanti numerici in base a N del GameModel.
     * Chiamato quando il pannello viene creato o quando N cambia (es. nuova partita).
     */
    public void updateControls() {
        buttonPanel.removeAll(); // Rimuovi i vecchi pulsanti

        int N = 0;
        boolean gameIsActive = false;
        if (gameModel != null) {
            N = gameModel.getN();
            gameIsActive = (gameModel.getGameState() == GameModel.GameState.PLAYING ||
                    gameModel.getGameState() == GameModel.GameState.CONSTRAINT_VIOLATION);
        }

        if (N > 0) {
            for (int i = 1; i <= N; i++) {
                final int number = i; // Necessario per l'uso nella lambda
                JButton numButton = new JButton(String.valueOf(number));
                styleNumberButton(numButton, false);
                numButton.setToolTipText("Inserisci il numero " + number);
                numButton.addActionListener((ActionEvent e) -> {
                    if (gameController != null) {
                        gameController.inputNumberViaButton(number);
                    }
                });
                numButton.setEnabled(gameIsActive); // Abilita solo se il gioco è attivo
                buttonPanel.add(numButton);
            }
        }

        if (clearButton != null) {
            clearButton.setEnabled(gameIsActive); // Abilita solo se il gioco è attivo
        }

        buttonPanel.revalidate();
        buttonPanel.repaint();
    }
}

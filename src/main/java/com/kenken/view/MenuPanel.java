package com.kenken.view;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel(Runnable newGameAction, Runnable loadGameAction,Runnable settingAction, Runnable exitAction) {
        setLayout(new GridBagLayout());
        setBackground(new Color(240,240,245));
        GridBagConstraints gbc = new GridBagConstraints();

        //Titolo del gioco
        JLabel titleLabel = new JLabel("KenKen Puzzle", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 48)); // Dimensione font aumentata
        titleLabel.setForeground(new Color(40, 40, 90)); // Colore più scuro per il titolo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 20, 50, 20); // Margine superiore e inferiore aumentati
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        // Configurazione comune per i pulsanti
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 50; // Più larghezza interna
        gbc.ipady = 20; // Più altezza interna
        gbc.insets = new Insets(15, 70, 15, 70); // Margini per i pulsanti

        // Pulsante Nuova Partita
        gbc.gridy = 1;
        JButton newGameButton = createStyledButton("Nuova Partita", newGameAction, new Color(70, 130, 180)); // Steel Blue
        add(newGameButton, gbc);

        // Pulsante Carica Partita
        gbc.gridy = 2;
        JButton loadGameButton = createStyledButton("Carica Partita", loadGameAction, new Color(100, 149, 237)); // Cornflower Blue
        add(loadGameButton, gbc);

        // Pulsante Impostazioni (disabilitato per ora)
        gbc.gridy = 3;
        JButton settingsButton = createStyledButton("Impostazioni", settingAction, new Color(176, 196, 222)); // Light Steel Blue
        settingsButton.setEnabled(false); // Abilita quando implementato
        add(settingsButton, gbc);

        // Pulsante Esci
        gbc.gridy = 4;
        gbc.insets = new Insets(25, 70, 25, 70); // Più margine sopra e sotto Esci
        JButton exitButton = createStyledButton("Esci", exitAction, new Color(255, 99, 71)); // Tomato Red
        add(exitButton, gbc);
    }

    private JButton createStyledButton(String text, Runnable action, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20)); // Font più grande
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1), // Bordo leggermente più scuro
                BorderFactory.createEmptyBorder(10, 25, 10, 25) // Padding interno
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursore a mano

        button.addActionListener(e -> action.run());

        // Effetto hover
        Color hoverColor = bgColor.brighter();
        Color pressedColor = bgColor.darker();

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(pressedColor);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (button.contains(evt.getPoint())) {
                    button.setBackground(hoverColor);
                } else {
                    button.setBackground(bgColor);
                }
            }
        });
        return button;
    }
}

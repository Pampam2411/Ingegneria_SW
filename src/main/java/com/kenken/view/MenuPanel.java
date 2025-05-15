package com.kenken.view;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel(Runnable newGameAction, Runnable loadGameAction, Runnable exitAction) {
        setLayout(new GridBagLayout());
        setBackground(new Color(240,240,245));
        GridBagConstraints gbc = new GridBagConstraints();

        //Titolo del gioco
        JLabel titleLabel = new JLabel("KenKen Puzzle", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 48));
        titleLabel.setForeground(new Color(40, 40, 90));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 20, 50, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        // Configurazione comune per i pulsanti
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 50;
        gbc.ipady = 20;
        gbc.insets = new Insets(15, 70, 15, 70);

        // Pulsante Nuova Partita
        gbc.gridy = 1;
        JButton newGameButton = createStyledButton("New Game", newGameAction, new Color(70, 130, 180));
        add(newGameButton, gbc);

        // Pulsante Carica Partita
        gbc.gridy = 2;
        JButton loadGameButton = createStyledButton("Load Game", loadGameAction, new Color(100, 149, 237));
        add(loadGameButton, gbc);

        // Pulsante Esci
        gbc.gridy = 4;
        gbc.insets = new Insets(25, 70, 25, 70);
        JButton exitButton = createStyledButton("Exit", exitAction, new Color(255, 99, 71));
        add(exitButton, gbc);
    }

    private JButton createStyledButton(String text, Runnable action, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(_ -> action.run());

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

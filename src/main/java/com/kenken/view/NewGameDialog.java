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

    private int selectedN = 0;
    private String selectedDifficulty = null;
    private boolean confirmed = false;

    public NewGameDialog(Frame owner) {
        super(owner, "Set Up New Game", true);
        initComponents();
        layoutComponents();
        addListeners();

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        Integer[] SIZES = {3, 4, 5, 6};
        sizeComboBox = new JComboBox<>(SIZES);
        sizeComboBox.setSelectedItem(3);

        String[] DIFFICULTIES = {
                PuzzleGenerator.DIFFICULTY_EASY,
                PuzzleGenerator.DIFFICULTY_MEDIUM,
                PuzzleGenerator.DIFFICULTY_HARD
        };
        difficultyComboBox = new JComboBox<>(DIFFICULTIES);
        difficultyComboBox.setSelectedItem(PuzzleGenerator.DIFFICULTY_EASY);

        startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(70, 130, 180)); // Steel Blue
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);


        cancelButton = new JButton("Cancel");
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
        JLabel sizeLabel = new JLabel("Grid Size (N x N):");
        sizeLabel.setFont(labelFont);
        panel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        sizeComboBox.setFont(labelFont);
        panel.add(sizeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(5, 5, 15, 10);
        JLabel difficultyLabel = new JLabel("Diffiulty Level:");
        difficultyLabel.setFont(labelFont);
        panel.add(difficultyLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        difficultyComboBox.setFont(labelFont);
        panel.add(difficultyComboBox, gbc);

        // Pannello per i pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(panel.getBackground());
        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 5, 5, 5);
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private void addListeners() {
        startButton.addActionListener((ActionEvent _) -> {
            selectedN = (Integer) Objects.requireNonNull(sizeComboBox.getSelectedItem());
            selectedDifficulty = (String) Objects.requireNonNull(difficultyComboBox.getSelectedItem());
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener((ActionEvent _) -> {
            confirmed = false;
            dispose();
        });
    }

    public int getSelectedN() {
        return selectedN;
    }

    public String getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

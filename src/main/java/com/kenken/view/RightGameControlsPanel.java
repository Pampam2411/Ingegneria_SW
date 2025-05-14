package com.kenken.view;

import com.kenken.controller.GameController;
import com.kenken.model.GameModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

public class RightGameControlsPanel extends JPanel { // Rinominato per chiarezza

    private GameController gameController;
    private GameModel gameModel;

    private JButton solveButton;
    private JButton validateButton;
    private JToggleButton realTimeValidationToggleButton;
    private JButton previousSolutionButton; // Nuovo
    private JButton nextSolutionButton;     // Nuovo
    private JLabel solutionNavigationLabel; // Nuovo

    public RightGameControlsPanel(GameController controller, GameModel model) {
        this.gameController = controller;
        this.gameModel = model;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                new EmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(220, 225, 230));

        JLabel titleLabel = new JLabel("Azioni Gioco");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 15)));

        solveButton = new JButton("Risolvi Puzzle");
        styleControlButton(solveButton);
        solveButton.addActionListener(e -> { if (gameController != null) gameController.solvePuzzle(); });
        add(solveButton);
        add(Box.createRigidArea(new Dimension(0, 10)));

        validateButton = new JButton("Valida Soluzione");
        styleControlButton(validateButton);
        validateButton.addActionListener(e -> { if (gameController != null) gameController.validateCurrentGrid(); });
        add(validateButton);
        add(Box.createRigidArea(new Dimension(0, 15)));

        realTimeValidationToggleButton = new JToggleButton();
        styleToggleButton(realTimeValidationToggleButton);
        realTimeValidationToggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (this.gameModel != null) updateToggleButtonState(this.gameModel.isRealTimeValidationEnabled());
        realTimeValidationToggleButton.addActionListener(e -> {
            if (gameController != null && gameModel != null) {
                boolean newSelectedState = realTimeValidationToggleButton.isSelected();
                if (gameModel.isRealTimeValidationEnabled() != newSelectedState) {
                    gameController.setRealTimeValidation(newSelectedState);
                }
            }
        });
        add(realTimeValidationToggleButton);
        add(Box.createRigidArea(new Dimension(0, 20))); // Spazio prima della navigazione

        // Controlli di Navigazione Soluzione
        solutionNavigationLabel = new JLabel("Naviga Soluzioni:");
        solutionNavigationLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        solutionNavigationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(solutionNavigationLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel navButtonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        navButtonContainer.setOpaque(false);

        previousSolutionButton = new JButton("< Prec");
        styleNavigationButton(previousSolutionButton);
        previousSolutionButton.addActionListener(e -> { if (gameController != null) gameController.showPreviousSolution(); });
        navButtonContainer.add(previousSolutionButton);

        nextSolutionButton = new JButton("Succ >");
        styleNavigationButton(nextSolutionButton);
        nextSolutionButton.addActionListener(e -> { if (gameController != null) gameController.showNextSolution(); });
        navButtonContainer.add(nextSolutionButton);
        add(navButtonContainer);


        add(Box.createVerticalGlue()); // Spinge i controlli in alto

        setPreferredSize(new Dimension(180, 320)); // Aggiusta altezza
        setMaximumSize(new Dimension(190, Short.MAX_VALUE));
    }

    private void styleControlButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 10, 5, 10));
        button.setMaximumSize(new Dimension(Short.MAX_VALUE, button.getPreferredSize().height + 5));
    }

    private void styleToggleButton(JToggleButton button) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setMargin(new Insets(5, 8, 5, 8));
        button.setMaximumSize(new Dimension(Short.MAX_VALUE, button.getPreferredSize().height + 5));
    }

    private void styleNavigationButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setMargin(new Insets(3, 6, 3, 6));
        button.setFocusPainted(false);
    }

    private void updateToggleButtonState(boolean isValidationEnabled) {
        if (realTimeValidationToggleButton != null) {
            realTimeValidationToggleButton.setSelected(isValidationEnabled);
            realTimeValidationToggleButton.setText(isValidationEnabled ? "Validazione: ATTIVA" : "Validazione: DISATTIVA");
        }
    }

    public void updateControlsState(GameModel model, GameController controller) {
        this.gameModel = model;
        this.gameController = controller;

        boolean isGameActiveOrInitialized = (model.getGameState() != GameModel.GameState.NOT_INITIALIZED &&
                model.getGameState() != GameModel.GameState.ERROR &&
                model.getN() > 0);
        boolean isGamePlaying = isGameActiveOrInitialized &&
                (model.getGameState() == GameModel.GameState.PLAYING ||
                        model.getGameState() == GameModel.GameState.CONSTRAINT_VIOLATION);
        boolean isGameSolved = model.getGameState() == GameModel.GameState.SOLVED;

        if (solveButton != null) solveButton.setEnabled(isGameActiveOrInitialized && !isGameSolved);
        if (validateButton != null) validateButton.setEnabled(isGamePlaying);
        if (realTimeValidationToggleButton != null) {
            realTimeValidationToggleButton.setEnabled(isGameActiveOrInitialized);
            updateToggleButtonState(model.isRealTimeValidationEnabled());
        }

        // Aggiorna stato e visibilità dei pulsanti di navigazione soluzione
        boolean solutionsAvailableAndDisplayed = false;
        if (controller != null) {
            int totalSolutions = controller.getTotalSolutionsFound();
            solutionsAvailableAndDisplayed = totalSolutions > 0 && isGameSolved;
        }

        if (solutionNavigationLabel != null) solutionNavigationLabel.setVisible(solutionsAvailableAndDisplayed);
        if (previousSolutionButton != null) {
            previousSolutionButton.setVisible(solutionsAvailableAndDisplayed);
            previousSolutionButton.setEnabled(solutionsAvailableAndDisplayed && controller != null && controller.canShowPreviousSolution());
        }
        if (nextSolutionButton != null) {
            nextSolutionButton.setVisible(solutionsAvailableAndDisplayed);
            nextSolutionButton.setEnabled(solutionsAvailableAndDisplayed && controller != null && controller.canShowNextSolution());
        }
    }
}

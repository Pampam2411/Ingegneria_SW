package com.kenken.view;

import com.kenken.controller.GameController;
import com.kenken.model.GameModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
public class RightGameControlsPanel extends JPanel {

    private GameController gameController;
    private GameModel gameModel;

    private final JButton solveButton;
    private final JToggleButton realTimeValidationToggleButton;
    private final JButton previousSolutionButton;
    private final JButton nextSolutionButton;
    private final JLabel solutionNavigationLabel;

    public RightGameControlsPanel(GameController controller, GameModel model) {
        this.gameController = controller;
        this.gameModel = model;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                new EmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(220, 225, 230));

        JLabel titleLabel = new JLabel("Action Game");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 15)));

        solveButton = new JButton("Solve Puzzle");
        styleControlButton(solveButton);
        solveButton.addActionListener(_ -> { if (gameController != null){
            String input = JOptionPane.showInputDialog(
                    RightGameControlsPanel.this,
                    "Enter the number of solutions to find:",
                    "Solve Options",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                int numSolutions = Integer.parseInt(input);
                gameController.solvePuzzle(numSolutions);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        RightGameControlsPanel.this,
                        "Invalid number entered.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }        } });
        add(solveButton);
        add(Box.createRigidArea(new Dimension(0, 10)));


        realTimeValidationToggleButton = new JToggleButton();
        styleToggleButton(realTimeValidationToggleButton);
        realTimeValidationToggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (this.gameModel != null) updateToggleButtonState(this.gameModel.isRealTimeValidationEnabled());
        realTimeValidationToggleButton.addActionListener(_ -> {
            if (gameController != null && gameModel != null) {
                boolean newSelectedState = realTimeValidationToggleButton.isSelected();
                if (gameModel.isRealTimeValidationEnabled() != newSelectedState) {
                    gameController.setRealTimeValidation(newSelectedState);
                }
            }
        });
        add(realTimeValidationToggleButton);
        add(Box.createRigidArea(new Dimension(0, 20)));


        solutionNavigationLabel = new JLabel("Browse Solution:");
        solutionNavigationLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        solutionNavigationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(solutionNavigationLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel navButtonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        navButtonContainer.setOpaque(false);

        previousSolutionButton = new JButton("< Prev");
        styleNavigationButton(previousSolutionButton);
        previousSolutionButton.addActionListener(_ -> { if (gameController != null) gameController.showPreviousSolution(); });
        navButtonContainer.add(previousSolutionButton);

        nextSolutionButton = new JButton("Next >");
        styleNavigationButton(nextSolutionButton);
        nextSolutionButton.addActionListener(_ -> { if (gameController != null) gameController.showNextSolution(); });
        navButtonContainer.add(nextSolutionButton);
        add(navButtonContainer);


        add(Box.createVerticalGlue());

        setPreferredSize(new Dimension(180, 320));
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
            realTimeValidationToggleButton.setText(isValidationEnabled ? "Validation:Active" : "Validation:Deactivate");
        }
    }

    public void updateControlsState(GameModel model, GameController controller) {
        this.gameModel = model;
        this.gameController = controller;

        boolean isGameActiveOrInitialized = (model.getGameState() != GameModel.GameState.NOT_INITIALIZED &&
                model.getGameState() != GameModel.GameState.ERROR &&
                model.getN() > 0);
        boolean isGameSolved = model.getGameState() == GameModel.GameState.SOLVED;

        if (solveButton != null) solveButton.setEnabled(isGameActiveOrInitialized && !isGameSolved);
        if (realTimeValidationToggleButton != null) {
            realTimeValidationToggleButton.setEnabled(isGameActiveOrInitialized);
            updateToggleButtonState(model.isRealTimeValidationEnabled());
        }

        boolean solutionsAvailableAndDisplayed = false;
        if (controller != null) {
            int totalSolutions = controller.getTotalSolutionsFound();
            solutionsAvailableAndDisplayed = totalSolutions > 0 && isGameSolved;
        }

        if (solutionNavigationLabel != null) solutionNavigationLabel.setVisible(solutionsAvailableAndDisplayed);
        if (previousSolutionButton != null) {
            previousSolutionButton.setVisible(solutionsAvailableAndDisplayed);
            previousSolutionButton.setEnabled(solutionsAvailableAndDisplayed && controller.canShowPreviousSolution());
        }
        if (nextSolutionButton != null) {
            nextSolutionButton.setVisible(solutionsAvailableAndDisplayed);
            nextSolutionButton.setEnabled(solutionsAvailableAndDisplayed && controller.canShowNextSolution());
        }
    }
}

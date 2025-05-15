package com.kenken.view;

import com.kenken.main.MainFrame;
import com.kenken.model.GameModel;
import com.kenken.controller.GameController;

import javax.swing.*;
import java.awt.*;

public class GameViewPanel extends JPanel {

    private final GridPanel gridPanel;
    private final LeftGameControlsPanel leftGameControlsPanel;
    private final RightGameControlsPanel rightGameControlsPanel;
    private final NumberInputPanel numberInputPanel;

    private final JLabel gameMessageLabel;
    private final JLabel feedbackLabel;

    private GameModel gameModel;
    private final GameController gameController;

    public GameViewPanel(MainFrame mainFrameInstance, GameModel model, GameController controller) {
        this.gameModel = model;
        this.gameController = controller;

        setLayout(new BorderLayout(10, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setBackground(new Color(230, 235, 240));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 5));
        centerPanel.setOpaque(false);

        gameMessageLabel = new JLabel(" ", SwingConstants.CENTER);
        gameMessageLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        gameMessageLabel.setOpaque(true);
        gameMessageLabel.setBackground(new Color(220, 220, 220, 200));
        gameMessageLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        gameMessageLabel.setVisible(false);
        centerPanel.add(gameMessageLabel, BorderLayout.NORTH);

        gridPanel = new GridPanel(model, controller);
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        centerPanel.add(gridPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        leftGameControlsPanel = new LeftGameControlsPanel(mainFrameInstance, controller, model);
        add(leftGameControlsPanel, BorderLayout.WEST);

        rightGameControlsPanel = new RightGameControlsPanel(controller, model);
        add(rightGameControlsPanel, BorderLayout.EAST);

        JPanel bottomAreaPanel = new JPanel(new BorderLayout(0, 3));
        bottomAreaPanel.setOpaque(false);

        numberInputPanel = new NumberInputPanel(controller, model);
        bottomAreaPanel.add(numberInputPanel, BorderLayout.NORTH);

        feedbackLabel = new JLabel("Pronto.", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
        feedbackLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 3, 0));
        bottomAreaPanel.add(feedbackLabel, BorderLayout.CENTER);

        add(bottomAreaPanel, BorderLayout.SOUTH);

        updatePanelState(this.gameModel);
    }

    public void updatePanelState(GameModel currentModel) {
        this.gameModel = currentModel;

        if (gridPanel != null) {
            gridPanel.setModel(this.gameModel);
        }
        if (leftGameControlsPanel != null) {
            leftGameControlsPanel.updateControlsState(this.gameModel);
        }
        if (rightGameControlsPanel != null) {
            rightGameControlsPanel.updateControlsState(this.gameModel, this.gameController);
        }
        if (numberInputPanel != null) {
            numberInputPanel.updateControls();
        }

        boolean isSolutionDisplayedBySolver = false;
        if (gameModel.getGameState() == GameModel.GameState.SOLVED) {
            if (gameController != null && gameController.getTotalSolutionsFound() > 0 && gameController.getCurrentSolutionIndex() != -1) {
                isSolutionDisplayedBySolver = true;
            }
        }

        gameMessageLabel.setVisible(false);

        switch (gameModel.getGameState()) {
            case SOLVED:
                if (isSolutionDisplayedBySolver) {
                    gameMessageLabel.setText("Show Solution " +
                            (gameController.getCurrentSolutionIndex() + 1) + " of " +
                            gameController.getTotalSolutionsFound() + ".");
                    gameMessageLabel.setBackground(new Color(200, 220, 255, 220));
                    gameMessageLabel.setForeground(Color.BLACK);
                } else {
                    gameMessageLabel.setText("Puzzle Solved!");
                    gameMessageLabel.setBackground(new Color(200, 255, 200, 220));
                    gameMessageLabel.setForeground(new Color(0, 100, 0));
                }
                gameMessageLabel.setVisible(true);
                break;
            case CONSTRAINT_VIOLATION:
                if (gameModel.isGridFull()) {
                    gameMessageLabel.setText("Puzzle Incorrect! Try Again.");
                    gameMessageLabel.setBackground(new Color(255, 200, 200, 220));
                    gameMessageLabel.setForeground(Color.RED.darker());
                    gameMessageLabel.setVisible(true);
                }
                break;
            case ERROR:
                String errorDetail = gameModel.getDifficulty();
                if (errorDetail != null && errorDetail.startsWith("ERROR")) {
                    gameMessageLabel.setText("Error: " + errorDetail.replace("ERROR_","").replace("_"," ") + ".");
                }
                gameMessageLabel.setBackground(new Color(255, 180, 180, 220));
                gameMessageLabel.setForeground(Color.BLACK);
                gameMessageLabel.setVisible(true);
                break;
            default:
                break;
        }

        if (feedbackLabel != null) {
            switch (gameModel.getGameState()) {
                case PLAYING:
                    feedbackLabel.setText("Running Game...");
                    feedbackLabel.setForeground(new Color(0, 100, 0));
                    break;
                case SOLVED:
                    feedbackLabel.setText("To start a new game, use the controls on the left.");
                    feedbackLabel.setForeground(Color.DARK_GRAY);
                    break;
                case CONSTRAINT_VIOLATION:
                    if (!gameModel.isGridFull()) {
                        feedbackLabel.setText("Warning: Violation of Constraints! Check the red cells.");
                        feedbackLabel.setForeground(Color.RED.darker());
                    } else {
                        feedbackLabel.setText("Correct the Highlighted Errors.");
                        feedbackLabel.setForeground(Color.RED.darker());
                    }
                    break;
                case ERROR:
                    feedbackLabel.setText("Warning: Error in Game!");
                    feedbackLabel.setForeground(Color.RED.darker());
                    break;
                case NOT_INITIALIZED:
                default:
                    feedbackLabel.setText("Ready to start a new game!");
                    feedbackLabel.setForeground(Color.DARK_GRAY);
                    break;
            }
        }
    }

    public GridPanel getGridPanel() {
        return this.gridPanel;
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result = super.requestFocusInWindow();
        if (gridPanel != null) {
            if (gridPanel.isFocusable() && gridPanel.isDisplayable() && gridPanel.isVisible()) {
                result = gridPanel.requestFocusInWindow();
            }
        }
        return result;
    }
}

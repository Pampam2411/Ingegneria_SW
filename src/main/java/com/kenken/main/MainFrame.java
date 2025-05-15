package com.kenken.main;

import com.kenken.model.GameModel;
import com.kenken.model.GameObserver;
import com.kenken.controller.GameController;
import com.kenken.view.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame implements GameObserver, UserFeedback {

    private GameModel gameModel;
    private final GameController gameController;

    private final JPanel mainPanelContainer;
    private final CardLayout cardLayout;
    private final GameViewPanel gameViewPanel;
    private static final String MENU_PANEL_ID = "MENU_PANEL";
    private static final String GAME_PANEL_ID = "GAME_PANEL";

    public MainFrame(GameModel model) {
        this.gameModel = model;
        this.gameController = new GameController(this.gameModel, this);
        this.gameModel.addObserver(this);

        setTitle("KenKen Puzzle Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanelContainer = new JPanel(cardLayout);

        MenuPanel menuPanel = new MenuPanel(
                this::handleNewGameRequestFromMenuPanel,
                this::handleLoadGameRequestFromMenuPanel,
                () -> System.exit(0)
        );
        mainPanelContainer.add(menuPanel, MENU_PANEL_ID);


        gameViewPanel = new GameViewPanel(this, this.gameModel, this.gameController);
        mainPanelContainer.add(gameViewPanel, GAME_PANEL_ID);

        add(mainPanelContainer, BorderLayout.CENTER);

        setJMenuBar(null);

        cardLayout.show(mainPanelContainer, MENU_PANEL_ID);

        setMinimumSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        update(this.gameModel);
    }

    @Override
    public void showErrorMessage(String title, String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE));
    }

    @Override
    public void showInfoMessage(String title, String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE));
    }

    public void switchToGameView() {
        if (gameModel.getGameState() == GameModel.GameState.NOT_INITIALIZED || gameModel.getN() == 0) {
            showErrorMessage("Game not Ready", "Set Up New Game First.");
            switchToMenuView();
            return;
        }
        setJMenuBar(null);
        gameViewPanel.updatePanelState(this.gameModel);
        GridPanel currentGridPanel = gameViewPanel.getGridPanel();
        if (currentGridPanel != null) {
            currentGridPanel.resetSelection();
            currentGridPanel.requestFocusInWindow();
        } else {
            gameViewPanel.requestFocusInWindow();
        }
        cardLayout.show(mainPanelContainer, GAME_PANEL_ID);
        revalidate();
        repaint();
    }

    public void switchToMenuView() {
        setJMenuBar(null);
        if (gameController != null) {
            gameController.clearFoundSolutions();
        }
        cardLayout.show(mainPanelContainer, MENU_PANEL_ID);
        revalidate();
        repaint();
    }

    public void handleNewGameRequestFromMenuPanel() {
        NewGameDialog newGameDialog = new NewGameDialog(this);
        newGameDialog.setVisible(true);
        if (newGameDialog.isConfirmed()) {
            int N = newGameDialog.getSelectedN();
            String diff = newGameDialog.getSelectedDifficulty();
            gameController.startNewGame(N, diff);
            if (gameModel.getGameState() != GameModel.GameState.NOT_INITIALIZED &&
                    gameModel.getGameState() != GameModel.GameState.ERROR &&
                    gameModel.getN() > 0) {
                switchToGameView();
            } else {
                switchToMenuView();
            }
        }
    }

    public void handleLoadGameRequestFromMenuPanel() {
        JFileChooser fileChooser = new JFileChooser();
        File loadDir = new File(LeftGameControlsPanel.SAVE_LOAD_GAME_DIRECTORY_PATH);
        if (loadDir.exists() && loadDir.isDirectory()) {
            fileChooser.setCurrentDirectory(loadDir);
        }
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (gameController.loadGame(selectedFile)) {
                if (gameModel.getGameState() != GameModel.GameState.NOT_INITIALIZED && gameModel.getN() > 0) {
                    switchToGameView();
                } else {
                    showErrorMessage("Loading Error", "Unable to view game after loading.");
                }
            }
        }
    }

    public void showGameRules() {
        String rules = """
                How to Play KenKen:
                
                1. Use numbers for 1 to N (grid size).
                2. Never use the same number for row or column.
                3. Each cage has an operation (+,-,*,/) and a target result.
                4. Numbers in a cage must achieve the target using the given operation.
                5. Repeated numbers are okay within a cage.
                """;
        showInfoMessage("Game Rules", rules);
    }

    public void showAboutInfo() {
        String about = "KenKen Puzzle Game\nVersion: 1.0 (Dev)";
        showInfoMessage("About KenKen Puzzle", about);
    }

    @Override
    public void update(GameModel model) {
        this.gameModel = model;
        SwingUtilities.invokeLater(this::performUpdateUI);
    }

    private void performUpdateUI() {
        if (gameViewPanel != null) {
            gameViewPanel.updatePanelState(this.gameModel);
        }


        if (gameController != null && gameController.getTotalSolutionsFound() > 0 && gameModel.getGameState() == GameModel.GameState.SOLVED) {
            setTitle("KenKen Puzzle - Solution " + (gameController.getCurrentSolutionIndex() + 1) + " of " + gameController.getTotalSolutionsFound());
        } else if (gameModel.getN() > 0 && gameModel.getGameState() != GameModel.GameState.NOT_INITIALIZED && gameModel.getGameState() != GameModel.GameState.ERROR) {
            setTitle("KenKen Puzzle - " + gameModel.getN() + "x" + gameModel.getN() + " (" + gameModel.getDifficulty() + ")");
        } else {
            setTitle("KenKen Puzzle Game");
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ex) { System.err.println("Error Look and Feel: " + ex.getMessage()); }
        }
        GameModel model = new GameModel();
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(model);
            frame.setVisible(true);
        });
    }
}

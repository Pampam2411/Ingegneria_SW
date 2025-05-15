package com.kenken.view;

import com.kenken.controller.GameController;
import com.kenken.main.MainFrame;
import com.kenken.model.GameModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class LeftGameControlsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final GameController gameController;
    private GameModel gameModel;

    private final JButton newGameButton;
    private final JButton loadGameButton;
    private final JButton saveGameButton;
    private final JButton mainMenuButton;
    private final JButton rulesButton;
    private final JButton aboutButton;

    public static final String SAVE_LOAD_GAME_DIRECTORY_PATH = "C:\\Users\\lucad\\Desktop\\SaveGameKenKen"; // Mantieni il tuo path

    public LeftGameControlsPanel(MainFrame frame, GameController controller, GameModel model) {
        this.mainFrame = frame;
        this.gameController = controller;
        this.gameModel = model;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                new EmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(220, 225, 230));

        JLabel titleLabel = new JLabel("Game & Help");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 15)));

        newGameButton = new JButton("New Game");
        styleControlButton(newGameButton);
        newGameButton.setToolTipText("Start New Game");
        newGameButton.addActionListener((ActionEvent _) -> {
            if (mainFrame != null) {
                mainFrame.handleNewGameRequestFromMenuPanel();
            }
        });
        add(newGameButton);
        add(Box.createRigidArea(new Dimension(0, 10)));

        loadGameButton = new JButton("Load Game");
        styleControlButton(loadGameButton);
        loadGameButton.setToolTipText("Load Saved Game");
        loadGameButton.addActionListener((ActionEvent _) -> handleLoadGameAction());
        add(loadGameButton);
        add(Box.createRigidArea(new Dimension(0, 10)));

        saveGameButton = new JButton("Save Game");
        styleControlButton(saveGameButton);
        saveGameButton.setToolTipText("Save Current Game");
        saveGameButton.addActionListener((ActionEvent _) -> handleSaveGameAction());
        add(saveGameButton);
        add(Box.createRigidArea(new Dimension(0, 20)));

        mainMenuButton = new JButton("Home");
        styleControlButton(mainMenuButton);
        mainMenuButton.setToolTipText("Back To Main Menu");
        mainMenuButton.addActionListener((ActionEvent _) -> {
            if (mainFrame != null) {
                mainFrame.switchToMenuView();
            }
        });
        add(mainMenuButton);
        add(Box.createRigidArea(new Dimension(0, 20)));

        rulesButton = new JButton("Games Rules");
        styleControlButton(rulesButton);
        rulesButton.addActionListener(_ -> {
            if (mainFrame != null) mainFrame.showGameRules();
        });
        add(rulesButton);
        add(Box.createRigidArea(new Dimension(0, 10)));

        aboutButton = new JButton("About");
        styleControlButton(aboutButton);
        aboutButton.addActionListener(_ -> {
            if (mainFrame != null) mainFrame.showAboutInfo();
        });
        add(aboutButton);

        add(Box.createVerticalGlue());

        setPreferredSize(new Dimension(180, 350));
        setMaximumSize(new Dimension(190, Short.MAX_VALUE));
    }

    private void styleControlButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 10, 5, 10));
        button.setMaximumSize(new Dimension(Short.MAX_VALUE, button.getPreferredSize().height + 5));
    }

    private void handleSaveGameAction() {
        if (gameController == null || gameModel == null || mainFrame == null) return;

        if (gameModel.getGameState() == GameModel.GameState.NOT_INITIALIZED || gameModel.getN() == 0) {
            mainFrame.showErrorMessage("Save Game", "No active game to save.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Game KenKen");
        fileChooser.setFileFilter(new FileNameExtensionFilter("File KenKen Saved As (*.ken)", "ken"));
        File saveDir = new File(SAVE_LOAD_GAME_DIRECTORY_PATH);
        if (saveDir.exists() && saveDir.isDirectory()) {
            fileChooser.setCurrentDirectory(saveDir);
        } else {
            System.out.println("Directory Save Default (" + SAVE_LOAD_GAME_DIRECTORY_PATH + ") Not Found.");
        }
        fileChooser.setSelectedFile(new File("kenken_game.ken"));
        int result = fileChooser.showSaveDialog(mainFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".ken")) {
                fileToSave = new File(filePath + ".ken");
            }
            gameController.saveGame(fileToSave);
        }
    }

    private void handleLoadGameAction() {
        if (gameController == null || mainFrame == null) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Game KenKen");
        fileChooser.setFileFilter(new FileNameExtensionFilter("File KenKen Saved (*.ken)", "ken"));
        File loadDir = new File(SAVE_LOAD_GAME_DIRECTORY_PATH);
        if (loadDir.exists() && loadDir.isDirectory()) {
            fileChooser.setCurrentDirectory(loadDir);
        } else {
            System.out.println("Directory Load Defualt (" + SAVE_LOAD_GAME_DIRECTORY_PATH + ") Not Found.");
        }
        int result = fileChooser.showOpenDialog(mainFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (gameController.loadGame(selectedFile)) {
                if (gameModel.getGameState() != GameModel.GameState.NOT_INITIALIZED && gameModel.getN() > 0) {
                    mainFrame.switchToGameView();
                } else {
                    mainFrame.showErrorMessage("Load Error", "Unable show game after loading.");
                }
            }
        }
    }


    public void updateControlsState(GameModel model) {
        this.gameModel = model;

        boolean isGameActiveOrInitialized = (model.getGameState() != GameModel.GameState.NOT_INITIALIZED &&
                model.getGameState() != GameModel.GameState.ERROR &&
                model.getN() > 0);
        boolean isGamePlaying = isGameActiveOrInitialized &&
                (model.getGameState() == GameModel.GameState.PLAYING ||
                        model.getGameState() == GameModel.GameState.CONSTRAINT_VIOLATION);

        if (newGameButton != null) newGameButton.setEnabled(true);
        if (loadGameButton != null) loadGameButton.setEnabled(true);
        if (saveGameButton != null) saveGameButton.setEnabled(isGamePlaying);
        if (mainMenuButton != null) mainMenuButton.setEnabled(true);
        if (rulesButton != null) rulesButton.setEnabled(true);
        if (aboutButton != null) aboutButton.setEnabled(true);
    }

}

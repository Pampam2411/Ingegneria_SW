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

    private MainFrame mainFrame; // Riferimento al MainFrame per cambiare vista o mostrare dialoghi
    private GameController gameController;
    private GameModel gameModel;

    private JButton newGameButton;
    private JButton loadGameButton;
    private JButton saveGameButton;
    private JButton mainMenuButton;
    private JButton rulesButton;
    private JButton aboutButton;

    public static final String SAVE_LOAD_GAME_DIRECTORY_PATH = "C:\\Users\\lucad\\OneDrive\\Desktop\\SaveGameKenKen"; // Mantieni il tuo path

    public LeftGameControlsPanel(MainFrame frame, GameController controller, GameModel model) {
        this.mainFrame = frame;
        this.gameController = controller;
        this.gameModel = model;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                new EmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(220, 225, 230)); // Sfondo simile al pannello destro

        JLabel titleLabel = new JLabel("Partita & Aiuto");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 15)));

        newGameButton = new JButton("Nuova Partita");
        styleControlButton(newGameButton);
        newGameButton.setToolTipText("Inizia una nuova partita KenKen");
        newGameButton.addActionListener((ActionEvent e) -> {
            if (mainFrame != null) {
                // L'handler in MainFrame gestirà il dialogo e il cambio di vista
                mainFrame.handleNewGameRequestFromMenuPanel();
            }
        });
        add(newGameButton);
        add(Box.createRigidArea(new Dimension(0, 10)));

        loadGameButton = new JButton("Carica Partita");
        styleControlButton(loadGameButton);
        loadGameButton.setToolTipText("Carica una partita salvata");
        loadGameButton.addActionListener((ActionEvent e) -> handleLoadGameAction());
        add(loadGameButton);
        add(Box.createRigidArea(new Dimension(0, 10)));

        saveGameButton = new JButton("Salva Partita");
        styleControlButton(saveGameButton);
        saveGameButton.setToolTipText("Salva la partita corrente");
        saveGameButton.addActionListener((ActionEvent e) -> handleSaveGameAction());
        add(saveGameButton);
        add(Box.createRigidArea(new Dimension(0, 20))); // Spazio maggiore

        mainMenuButton = new JButton("Menu Principale");
        styleControlButton(mainMenuButton);
        mainMenuButton.setToolTipText("Torna al menu principale");
        mainMenuButton.addActionListener((ActionEvent e) -> {
            if (mainFrame != null) {
                mainFrame.switchToMenuView();
            }
        });
        add(mainMenuButton);
        add(Box.createRigidArea(new Dimension(0, 20)));

        rulesButton = new JButton("Regole del Gioco");
        styleControlButton(rulesButton);
        rulesButton.addActionListener(e -> {
            if (mainFrame != null) mainFrame.showGameRules();
        });
        add(rulesButton);
        add(Box.createRigidArea(new Dimension(0, 10)));

        aboutButton = new JButton("Informazioni");
        styleControlButton(aboutButton);
        aboutButton.addActionListener(e -> {
            if (mainFrame != null) mainFrame.showAboutInfo();
        });
        add(aboutButton);

        add(Box.createVerticalGlue()); // Spinge i controlli in alto

        setPreferredSize(new Dimension(180, 350)); // Aggiusta la dimensione preferita
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
            mainFrame.showErrorMessage("Salva Partita", "Nessuna partita attiva da salvare.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salva Partita KenKen");
        fileChooser.setFileFilter(new FileNameExtensionFilter("File KenKen Salvati (*.ken)", "ken"));
        File saveDir = new File(SAVE_LOAD_GAME_DIRECTORY_PATH);
        if (saveDir.exists() && saveDir.isDirectory()) {
            fileChooser.setCurrentDirectory(saveDir);
        } else {
            System.out.println("Directory salvataggio predefinita (" + SAVE_LOAD_GAME_DIRECTORY_PATH + ") non trovata.");
        }
        fileChooser.setSelectedFile(new File("kenken_partita.ken"));
        int result = fileChooser.showSaveDialog(mainFrame); // Usa mainFrame come parent
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
        fileChooser.setDialogTitle("Carica Partita KenKen");
        fileChooser.setFileFilter(new FileNameExtensionFilter("File KenKen Salvati (*.ken)", "ken"));
        File loadDir = new File(SAVE_LOAD_GAME_DIRECTORY_PATH);
        if (loadDir.exists() && loadDir.isDirectory()) {
            fileChooser.setCurrentDirectory(loadDir);
        } else {
            System.out.println("Directory caricamento predefinita (" + SAVE_LOAD_GAME_DIRECTORY_PATH + ") non trovata.");
        }
        int result = fileChooser.showOpenDialog(mainFrame); // Usa mainFrame come parent
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (gameController.loadGame(selectedFile)) {
                if (gameModel.getGameState() != GameModel.GameState.NOT_INITIALIZED && gameModel.getN() > 0) {
                    mainFrame.switchToGameView(); // Assicura che la vista si aggiorni
                } else {
                    mainFrame.showErrorMessage("Errore Caricamento", "Impossibile visualizzare la partita dopo il caricamento.");
                }
            }
            // Se il caricamento fallisce, GameController dovrebbe aver mostrato un messaggio tramite UserFeedback (MainFrame)
        }
    }


    public void updateControlsState(GameModel model) {
        this.gameModel = model; // Aggiorna riferimento al modello

        boolean isGameActiveOrInitialized = (model.getGameState() != GameModel.GameState.NOT_INITIALIZED &&
                model.getGameState() != GameModel.GameState.ERROR &&
                model.getN() > 0);
        boolean isGamePlaying = isGameActiveOrInitialized &&
                (model.getGameState() == GameModel.GameState.PLAYING ||
                        model.getGameState() == GameModel.GameState.CONSTRAINT_VIOLATION);

        if (newGameButton != null) newGameButton.setEnabled(true); // Sempre abilitato
        if (loadGameButton != null) loadGameButton.setEnabled(true); // Sempre abilitato
        if (saveGameButton != null) saveGameButton.setEnabled(isGamePlaying);
        if (mainMenuButton != null) mainMenuButton.setEnabled(true); // Sempre abilitato
        if (rulesButton != null) rulesButton.setEnabled(true); // Sempre abilitato
        if (aboutButton != null) aboutButton.setEnabled(true); // Sempre abilitato
    }

}

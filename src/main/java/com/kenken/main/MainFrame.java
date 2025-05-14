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
    private GameController gameController;

    private JPanel mainPanelContainer;
    private CardLayout cardLayout;
    private MenuPanel menuPanel;
    private GameViewPanel gameViewPanel;
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

        menuPanel = new MenuPanel(
                this::handleNewGameRequestFromMenuPanel,
                this::handleLoadGameRequestFromMenuPanel,
                this::handleSettingsRequest,
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
            showErrorMessage("Gioco non Pronto", "Nessuna partita valida inizializzata.\nConfigura una nuova partita.");
            switchToMenuView(); // Assicura che si torni al menu
            return;
        }
        setJMenuBar(null); // Assicura che non ci sia JMenuBar
        gameViewPanel.updatePanelState(this.gameModel);
        GridPanel currentGridPanel = gameViewPanel.getGridPanel();
        if (currentGridPanel != null) {
            currentGridPanel.resetSelection();
            currentGridPanel.requestFocusInWindow();
        } else if (gameViewPanel != null) {
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

    // Questi handler sono ora chiamati dai pulsanti nel LeftGameControlsPanel
    // Il MainFrame li espone perché LeftGameControlsPanel ha un riferimento a MainFrame
    public void handleNewGameRequestFromMenuPanel() { // Rinominato per chiarezza, ma la logica è la stessa
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
                // L'errore specifico dovrebbe essere già stato mostrato dal GameController
                switchToMenuView(); // Assicura che si torni al menu
            }
        }
    }

    public void handleLoadGameRequestFromMenuPanel() { // Rinominato per chiarezza
        JFileChooser fileChooser = new JFileChooser();
        // ... (logica JFileChooser come prima, ma ora chiamata da LeftGameControlsPanel) ...
        // Imposta la directory di caricamento predefinita
        File loadDir = new File(LeftGameControlsPanel.SAVE_LOAD_GAME_DIRECTORY_PATH); // Usa la costante da LeftGameControlsPanel
        if (loadDir.exists() && loadDir.isDirectory()) {
            fileChooser.setCurrentDirectory(loadDir);
        }
        // ...
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (gameController.loadGame(selectedFile)) {
                if (gameModel.getGameState() != GameModel.GameState.NOT_INITIALIZED && gameModel.getN() > 0) {
                    switchToGameView();
                } else {
                    showErrorMessage("Errore Caricamento", "Impossibile visualizzare la partita dopo il caricamento.");
                }
            }
        }
    }

    // Questo metodo è per il pulsante "Impostazioni" nel MenuPanel iniziale
    public void handleSettingsRequest() {
        showInfoMessage("Impostazioni", "Funzionalità 'Impostazioni' non ancora implementata.");
    }

    // Questi metodi sono per i pulsanti "Regole" e "Informazioni" nel LeftGameControlsPanel
    public void showGameRules() {
        String rules = "Regole del KenKen:\n\n" +
                "1. Completa la griglia con numeri da 1 a N.\n" +
                "2. Non ripetere numeri nella stessa riga o colonna.\n" +
                "3. Ogni 'gabbia' ha un 'target' e un'operazione.\n" +
                "4. I numeri in una gabbia devono produrre il target.\n" +
                "5. I numeri possono ripetersi in una gabbia (non su stessa riga/colonna).";
        showInfoMessage("Regole del Gioco KenKen", rules);
    }

    public void showAboutInfo() {
        String about = "KenKen Puzzle Game\nVersione: 1.0 (Dev)";
        showInfoMessage("Informazioni su KenKen Puzzle", about);
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

        // Non c'è più una JMenuBar di gioco da aggiornare,
        // lo stato dei controlli è gestito dai pannelli laterali.

        // Aggiorna il titolo della finestra
        if (gameController != null && gameController.getTotalSolutionsFound() > 0 && gameModel.getGameState() == GameModel.GameState.SOLVED) {
            setTitle("KenKen Puzzle - Soluzione " + (gameController.getCurrentSolutionIndex() + 1) + " di " + gameController.getTotalSolutionsFound());
        } else if (gameModel.getN() > 0 && gameModel.getGameState() != GameModel.GameState.NOT_INITIALIZED && gameModel.getGameState() != GameModel.GameState.ERROR) {
            setTitle("KenKen Puzzle - " + gameModel.getN() + "x" + gameModel.getN() + " (" + gameModel.getDifficulty() + ")");
        } else {
            setTitle("KenKen Puzzle Game");
        }
    }

    public static void main(String[] args) {
        // ... (codice LookAndFeel e avvio come prima) ...
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ex) { System.err.println("Errore Look and Feel: " + ex.getMessage()); }
        }
        GameModel model = new GameModel();
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(model);
            frame.setVisible(true);
        });
    }
}

package com.kenken.view;

import com.kenken.main.MainFrame;
import com.kenken.model.GameModel;
import com.kenken.controller.GameController;

import javax.swing.*;
import java.awt.*;

public class GameViewPanel extends JPanel {

    private GridPanel gridPanel;
    private LeftGameControlsPanel leftGameControlsPanel;
    private RightGameControlsPanel rightGameControlsPanel;
    private NumberInputPanel numberInputPanel; // Nuovo pannello per i numeri

    // Le etichette di stato ora potrebbero essere rimosse o integrate diversamente
    // private JLabel statusLabel, difficultyLabel, feedbackLabel, validationStatusLabelInStatus;
    // private JLabel solutionInfoLabelInStatus;

    private GameModel gameModel;
    private GameController gameController;
    private MainFrame mainFrame; // Necessario per LeftGameControlsPanel

    public GameViewPanel(MainFrame mainFrame, GameModel model, GameController controller) {
        this.mainFrame = mainFrame;
        this.gameModel = model;
        this.gameController = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setBackground(new Color(230, 235, 240));

        gridPanel = new GridPanel(model, controller);
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        add(gridPanel, BorderLayout.CENTER);

        leftGameControlsPanel = new LeftGameControlsPanel(mainFrame, controller, model);
        add(leftGameControlsPanel, BorderLayout.WEST);

        rightGameControlsPanel = new RightGameControlsPanel(controller, model);
        add(rightGameControlsPanel, BorderLayout.EAST);

        // Crea e aggiungi il NumberInputPanel in basso
        numberInputPanel = new NumberInputPanel(controller, model);
        add(numberInputPanel, BorderLayout.SOUTH);

        updatePanelState(this.gameModel);
    }

    // Rimuovi setupStatusPanelLocal() se non più usato.
    // La logica delle etichette di stato è stata rimossa per fare spazio alla barra numerica.
    // Se vuoi mantenere alcune info, dovrai trovare un nuovo posto (es. nel titolo della finestra,
    // o un piccolo pannello sopra/sotto la griglia, o integrarle nei pannelli laterali).

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
            numberInputPanel.updateControls(); // Aggiorna i pulsanti numerici (es. se N cambia)
        }

        // La logica per aggiornare le etichette di stato (statusLabel, difficultyLabel, ecc.)
        // è stata rimossa da qui perché il pannello di stato è stato sostituito.
        // Se vuoi mantenere queste informazioni, dovrai riposizionarle.
        // Ad esempio, il titolo della finestra viene già aggiornato in MainFrame.performUpdateUI().
        // Il feedbackLabel potrebbe essere spostato in uno dei pannelli laterali o
        // mostrato tramite JOptionPane dal UserFeedback.
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

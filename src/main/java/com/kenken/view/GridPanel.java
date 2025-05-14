package com.kenken.view;

import com.kenken.model.Cage;
import com.kenken.model.Cell;
import com.kenken.model.GameModel;
import com.kenken.model.OperationType;
import com.kenken.model.dto.Coordinates;
import com.kenken.controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class GridPanel extends JPanel {

    private GameModel gameModel;
    private GameController gameController;

    private int cellSize;
    private int margin;
    private int offsetX;
    private int offsetY;

    private Coordinates selectedCellCoord;

    private static final Color GRID_LINE_COLOR = Color.BLACK;
    private static final Color CAGE_LINE_COLOR = Color.BLACK;
    private static final Color CELL_VALUE_COLOR = Color.BLUE.darker();
    private static final Color FIXED_CELL_VALUE_COLOR = Color.BLACK;
    private static final Color CAGE_LABEL_COLOR = new Color(20, 20, 20);
    private static final Color SELECTED_CELL_BORDER_COLOR = new Color(70, 130, 180, 220);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color GRID_BACKGROUND_COLOR = Color.WHITE;
    private static final Color VIOLATING_CELL_BACKGROUND_COLOR = new Color(255, 150, 150, 180);

    private static final Font CELL_VALUE_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font CAGE_LABEL_FONT = new Font("Arial", Font.BOLD, 13);

    private static final Stroke GRID_STROKE = new BasicStroke(1);
    private static final Stroke CAGE_STROKE = new BasicStroke(3);
    private static final Stroke SELECTED_CELL_STROKE = new BasicStroke(3);

    private static final List<Color> CAGE_BACKGROUND_PALETTE = new ArrayList<>();
    static {
        CAGE_BACKGROUND_PALETTE.add(new Color(255, 224, 189, 150)); CAGE_BACKGROUND_PALETTE.add(new Color(173, 216, 230, 150));
        CAGE_BACKGROUND_PALETTE.add(new Color(144, 238, 144, 150)); CAGE_BACKGROUND_PALETTE.add(new Color(255, 250, 205, 150));
        CAGE_BACKGROUND_PALETTE.add(new Color(221, 160, 221, 150)); CAGE_BACKGROUND_PALETTE.add(new Color(240, 230, 140, 150));
        CAGE_BACKGROUND_PALETTE.add(new Color(175, 238, 238, 150)); CAGE_BACKGROUND_PALETTE.add(new Color(255, 192, 203, 150));
        CAGE_BACKGROUND_PALETTE.add(new Color(211, 211, 211, 150)); CAGE_BACKGROUND_PALETTE.add(new Color(255, 239, 213, 150));
        CAGE_BACKGROUND_PALETTE.add(new Color(176, 224, 230, 150)); CAGE_BACKGROUND_PALETTE.add(new Color(224, 255, 255, 150));
    }

    public GridPanel(GameModel model, GameController controller) {
        this.gameModel = model;
        this.gameController = controller;
        this.margin = 25;
        this.selectedCellCoord = null;
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(450, 450));
        setFocusable(true);
        addMouseListener(new GridMouseListener());
        addKeyListener(new GridKeyListener());
    }

    public void setModel(GameModel model) {
        this.gameModel = model;
        // Non resettare selectedCellCoord qui, ma notifica il controller che non c'è selezione
        // se il modello cambia in modo significativo (es. nuovo gioco).
        // Il reset della selezione è gestito in resetSelection().
        if (model != null && model.getN() > 0 && getComponentCount() > 0 && getComponent(0) instanceof JLabel) {
            removeAll(); revalidate();
        } else if ((model == null || model.getN() == 0) && getComponentCount() == 0) {
            addPlaceholderLabel(); revalidate();
        }
        repaint();
    }

    public void resetSelection() {
        this.selectedCellCoord = null;
        if (gameController != null) {
            gameController.setActiveCellFromGrid(null); // Notifica il controller
        }
        repaint();
    }

    private void addPlaceholderLabel() {
        setLayout(new BorderLayout());
        JLabel placeholderLabel = new JLabel(
                "<html><div style='text-align: center; padding: 20px;'>" +
                        "Area Griglia di Gioco<br>" +
                        "Configura una nuova partita per visualizzare la griglia." +
                        "</div></html>",
                SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        placeholderLabel.setForeground(Color.DARK_GRAY);
        add(placeholderLabel, BorderLayout.CENTER);
    }

    private void calculateDimensions() {
        if (gameModel == null || gameModel.getN() == 0) {
            cellSize = 0; return;
        }
        int N = gameModel.getN();
        int panelWidth = getWidth(); int panelHeight = getHeight();
        int availableWidth = panelWidth - 2 * margin; int availableHeight = panelHeight - 2 * margin;
        cellSize = Math.min(availableWidth / N, availableHeight / N);
        if (cellSize <= 0) cellSize = 1;
        int gridWidth = N * cellSize; int gridHeight = N * cellSize;
        offsetX = (panelWidth - gridWidth) / 2; offsetY = (panelHeight - gridHeight) / 2;
    }

    private class GridMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (gameModel == null || gameModel.getN() == 0 || cellSize == 0) {
                selectedCellCoord = null;
                if (gameController != null) gameController.setActiveCellFromGrid(null);
                repaint(); return;
            }
            if (!isFocusOwner()) requestFocusInWindow();
            int N = gameModel.getN();
            Coordinates oldSelection = selectedCellCoord;
            if (e.getX() >= offsetX && e.getX() < offsetX + N * cellSize &&
                    e.getY() >= offsetY && e.getY() < offsetY + N * cellSize) {
                int col = (e.getX() - offsetX) / cellSize;
                int row = (e.getY() - offsetY) / cellSize;
                if (row >= 0 && row < N && col >= 0 && col < N) {
                    Cell clickedCell = gameModel.getCell(row, col);
                    if (clickedCell != null && clickedCell.isEditable()) {
                        selectedCellCoord = new Coordinates(row, col);
                    } else {
                        selectedCellCoord = null;
                    }
                } else selectedCellCoord = null;
            } else selectedCellCoord = null;

            // Notifica il controller solo se la selezione è cambiata
            if ( (oldSelection == null && selectedCellCoord != null) ||
                    (oldSelection != null && !oldSelection.equals(selectedCellCoord)) ||
                    (oldSelection != null && selectedCellCoord == null) ) {
                if (gameController != null) gameController.setActiveCellFromGrid(selectedCellCoord);
            }
            repaint();
        }
    }

    private class GridKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (selectedCellCoord == null || gameModel == null || gameModel.getN() == 0 || gameController == null) return;
            int N = gameModel.getN(); int keyCode = e.getKeyCode();
            int currentRow = selectedCellCoord.row(); int currentCol = selectedCellCoord.col();
            boolean numberPressed = false; int number = -1;

            if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_0 + N) {
                number = keyCode - KeyEvent.VK_0;
                if (keyCode == KeyEvent.VK_0 && N >=10) number = 0;
                else if (keyCode == KeyEvent.VK_0 && N < 10) return;
                else if (keyCode > KeyEvent.VK_0 + N && !(N>=10 && number ==0) ) return;
                if (number >= (N>=10 && number == 0 ? 0 : 1) && number <=N) numberPressed = true;
            } else if (keyCode >= KeyEvent.VK_NUMPAD1 && keyCode <= KeyEvent.VK_NUMPAD0 + N) {
                number = keyCode - KeyEvent.VK_NUMPAD0;
                if (keyCode == KeyEvent.VK_NUMPAD0 && N >=10) number = 0;
                else if (keyCode == KeyEvent.VK_NUMPAD0 && N < 10) return;
                else if (keyCode > KeyEvent.VK_NUMPAD0 + N && !(N>=10 && number == 0)) return;
                if (number >= (N>=10 && number == 0 ? 0 : 1) && number <=N) numberPressed = true;
            }

            if (numberPressed) {
                gameController.placeNumberInCell(currentRow, currentCol, number); // Il controller usa la cella selezionata se necessario
                // repaint(); // Il modello notificherà, causando un repaint generale
            } else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_0 || keyCode == KeyEvent.VK_NUMPAD0) {
                if (N < 10 && (keyCode == KeyEvent.VK_0 || keyCode == KeyEvent.VK_NUMPAD0)) {
                    gameController.clearCell(currentRow, currentCol); // repaint();
                } else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                    gameController.clearCell(currentRow, currentCol); // repaint();
                }
            } else {
                int nextRow = currentRow, nextCol = currentCol; boolean moved = false;
                switch (keyCode) {
                    case KeyEvent.VK_UP:    if (currentRow > 0) { nextRow--; moved = true; } break;
                    case KeyEvent.VK_DOWN:  if (currentRow < N - 1) { nextRow++; moved = true; } break;
                    case KeyEvent.VK_LEFT:  if (currentCol > 0) { nextCol--; moved = true; } break;
                    case KeyEvent.VK_RIGHT: if (currentCol < N - 1) { nextCol++; moved = true; } break;
                }
                if (moved) {
                    Cell targetCell = gameModel.getCell(nextRow, nextCol);
                    if (targetCell != null && targetCell.isEditable()) {
                        selectedCellCoord = new Coordinates(nextRow, nextCol);
                        if (gameController != null) gameController.setActiveCellFromGrid(selectedCellCoord); // Notifica il controller
                        repaint();
                    }
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        calculateDimensions();
        if (gameModel == null || gameModel.getN() == 0 || cellSize == 0) {
            if (getComponentCount() == 0) {
                g2d.setColor(Color.DARK_GRAY); g2d.setFont(new Font("Arial", Font.ITALIC, 16));
                String msg = "Nessuna partita attiva. Configura una nuova partita.";
                FontMetrics fm = g2d.getFontMetrics(); int msgWidth = fm.stringWidth(msg);
                g2d.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
            }
            return;
        }
        int N = gameModel.getN();
        g2d.setColor(GRID_BACKGROUND_COLOR);
        g2d.fillRect(offsetX, offsetY, N * cellSize, N * cellSize);
        drawCageBackgrounds(g2d, N);
        drawViolatingCellBackgrounds(g2d, N);
        drawCellValues(g2d, N);
        drawGridLines(g2d, N);
        drawCageOutlinesAndLabels(g2d, N);
        drawSelectedCellHighlight(g2d);
    }

    private void drawCageBackgrounds(Graphics2D g2d, int N) {
        if (gameModel.getCages() == null || CAGE_BACKGROUND_PALETTE.isEmpty()) return;
        int cageIndex = 0;
        for (Cage cage : gameModel.getCages()) {
            if (cage.getCellsInCage() == null || cage.getCellsInCage().isEmpty()) continue;
            Color cageColor = CAGE_BACKGROUND_PALETTE.get(cage.getCageId() % CAGE_BACKGROUND_PALETTE.size());
            g2d.setColor(cageColor);
            for (Cell cellInCage : cage.getCellsInCage()) {
                g2d.fillRect(offsetX + cellInCage.getCol() * cellSize, offsetY + cellInCage.getRow() * cellSize, cellSize, cellSize);
            }
            cageIndex++;
        }
    }

    private void drawViolatingCellBackgrounds(Graphics2D g2d, int N) {
        if (gameModel.isRealTimeValidationEnabled() &&
                gameModel.getGameState() == GameModel.GameState.CONSTRAINT_VIOLATION &&
                gameModel.getViolatingCells() != null) {
            Set<Coordinates> errorCells = gameModel.getViolatingCells();
            if (!errorCells.isEmpty()) {
                g2d.setColor(VIOLATING_CELL_BACKGROUND_COLOR);
                for (Coordinates coord : errorCells) {
                    if (coord.row() >= 0 && coord.row() < N && coord.col() >= 0 && coord.col() < N) {
                        g2d.fillRect(offsetX + coord.col() * cellSize, offsetY + coord.row() * cellSize, cellSize, cellSize);
                    }
                }
            }
        }
    }

    private void drawGridLines(Graphics2D g2d, int N) {
        g2d.setColor(GRID_LINE_COLOR); g2d.setStroke(GRID_STROKE);
        for (int i = 0; i <= N; i++) {
            g2d.drawLine(offsetX, offsetY + i * cellSize, offsetX + N * cellSize, offsetY + i * cellSize);
            g2d.drawLine(offsetX + i * cellSize, offsetY, offsetX + i * cellSize, offsetY + N * cellSize);
        }
    }

    private void drawCellValues(Graphics2D g2d, int N) {
        g2d.setFont(CELL_VALUE_FONT); FontMetrics fm = g2d.getFontMetrics();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                Cell cell = gameModel.getCell(r, c);
                if (cell != null && !cell.isEmpty()) {
                    String valueStr = String.valueOf(cell.getValue());
                    int textWidth = fm.stringWidth(valueStr); int textHeight = fm.getAscent();
                    int x = offsetX + c * cellSize + (cellSize - textWidth) / 2;
                    int y = offsetY + r * cellSize + (cellSize - textHeight) / 2 + fm.getAscent() - (fm.getDescent()/2);
                    g2d.setColor(cell.isEditable() ? CELL_VALUE_COLOR : FIXED_CELL_VALUE_COLOR);
                    g2d.drawString(valueStr, x, y);
                }
            }
        }
    }

    private void drawCageOutlinesAndLabels(Graphics2D g2d, int N) {
        if (gameModel.getCages() == null) return;
        g2d.setColor(CAGE_LINE_COLOR); g2d.setStroke(CAGE_STROKE);
        for (Cage cage : gameModel.getCages()) {
            if (cage.getCellsInCage() == null || cage.getCellsInCage().isEmpty()) continue;
            for (Cell cellInCage : cage.getCellsInCage()) {
                int r = cellInCage.getRow(); int c = cellInCage.getCol();
                if (r == 0 || !isCellInSameCage(r - 1, c, cage)) g2d.drawLine(offsetX + c * cellSize, offsetY + r * cellSize, offsetX + (c + 1) * cellSize, offsetY + r * cellSize);
                if (r == N - 1 || !isCellInSameCage(r + 1, c, cage)) g2d.drawLine(offsetX + c * cellSize, offsetY + (r + 1) * cellSize, offsetX + (c + 1) * cellSize, offsetY + (r + 1) * cellSize);
                if (c == 0 || !isCellInSameCage(r, c - 1, cage)) g2d.drawLine(offsetX + c * cellSize, offsetY + r * cellSize, offsetX + c * cellSize, offsetY + (r + 1) * cellSize);
                if (c == N - 1 || !isCellInSameCage(r, c + 1, cage)) g2d.drawLine(offsetX + (c + 1) * cellSize, offsetY + r * cellSize, offsetX + (c + 1) * cellSize, offsetY + (r + 1) * cellSize);
            }
            Cell firstCell = null; int minRow = N + 1, minCol = N + 1;
            for(Cell cell : cage.getCellsInCage()){
                if(cell.getRow() < minRow){ minRow = cell.getRow(); minCol = cell.getCol(); firstCell = cell;
                } else if (cell.getRow() == minRow && cell.getCol() < minCol){ minCol = cell.getCol(); firstCell = cell;}
            }
            if (firstCell != null) {
                String label = String.valueOf(cage.getTargetValue());
                if (cage.getOperationType() != OperationType.NONE) label += cage.getOperationType().getSymbol();
                g2d.setFont(CAGE_LABEL_FONT); g2d.setColor(CAGE_LABEL_COLOR); FontMetrics fmCage = g2d.getFontMetrics();
                g2d.drawString(label, offsetX + minCol * cellSize + 4, offsetY + minRow * cellSize + fmCage.getAscent() + 2);
            }
        }
    }

    private boolean isCellInSameCage(int r, int c, Cage currentCage) {
        if (gameModel == null || gameModel.getGrid() == null) return false;
        if (r < 0 || r >= gameModel.getN() || c < 0 || c >= gameModel.getN()) return false;
        Cell otherCell = gameModel.getCell(r, c);
        return otherCell != null && otherCell.getParentCage() != null && currentCage != null && otherCell.getParentCage().equals(currentCage);
    }

    private void drawSelectedCellHighlight(Graphics2D g2d) {
        if (selectedCellCoord != null && cellSize > 0) {
            g2d.setColor(SELECTED_CELL_BORDER_COLOR); g2d.setStroke(SELECTED_CELL_STROKE);
            g2d.drawRect(offsetX + selectedCellCoord.col() * cellSize +1, offsetY + selectedCellCoord.row() * cellSize +1, cellSize -2, cellSize -2);
        }
    }
}

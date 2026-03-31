package com.example.a2048;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Game {
    private ArrayList<ArrayList<Cell>> cells;
    private int gridSize;

    private ArrayList<ArrayList<Cell>> previousCells;
    private int previousScore;
    private int currentScore;
    private int stopScore;
    private int moves = 0;

    public Game(int gridSize) {
        this.gridSize = gridSize;
        this.stopScore = 2024;
        this.cells = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            ArrayList<Cell> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {
                row.add(new Cell());
            }
            this.cells.add(row);
        }
    }

    public int getCellVal(int row, int col) {
        return cells.get(row).get(col).getVal();
    }

    public void setCellVal(int row, int col, int val) {
        cells.get(row).get(col).setVal(val);
    }

    public int getGridSize() {
        return gridSize;
    }


    ////////// GAME LOGIC ///////////////
    // slide all non-zero cells to one side ->  merge adjacent equal cells -> slide again to close gaps from merging
    public int moveLeft()  {
        saveState();
        return applyMove(false, false);
    }
    public int moveRight() {
        saveState();
        return applyMove(false, true);
    }
    public int moveUp()    {
        saveState();
        return applyMove(true,  false);
    }
    public int moveDown()  {
        saveState();
        return applyMove(true,  true);
    }

    // transpose=true treats columns as rows (handles up/down)
    // reverse=true processes right-to-left (handles right/down)
    private int applyMove(boolean transpose, boolean reverse) {
        int score = 0;

        for (int i = 0; i < gridSize; i++) {
            // Extract the line (row or column) we're working on
            int[] line = getLine(i, transpose, reverse);

            // Step 1: slide non-zero values to the left
            int[] slid = slide(line);

            // Step 2: merge adjacent equal values
            int[] merged = merge(slid);
            score += merged[gridSize]; // last slot holds the score delta

            // Step 3: slide again to close gaps left by merging
            int[] result = slide(merged);

            // Write the result back
            setLine(i, result, transpose, reverse);
        }

        if (!boardChanged()) {
            undo(); // roll back cells and score
            return -1;
        }

        moves++;
        return score;
    }

    // Extract a row or column as a flat array
    // The extra slot [gridSize] is used to carry score out of merge()
    private int[] getLine(int index, boolean transpose, boolean reverse) {
        int[] line = new int[gridSize + 1];
        for (int j = 0; j < gridSize; j++) {
            int col = transpose ? j     : index;
            int row = transpose ? index : j;
            int pos = reverse ? (gridSize - 1 - j) : j;
            line[pos] = cells.get(col).get(row).getVal();
        }
        return line;
    }

    // Write a flat array back into the grid
    private void setLine(int index, int[] line, boolean transpose, boolean reverse) {
        for (int j = 0; j < gridSize; j++) {
            int row = transpose ? j     : index;
            int col = transpose ? index : j;
            int pos = reverse ? (gridSize - 1 - j) : j;
            cells.get(row).get(col).setVal(line[pos]);
        }
    }

    // Pack all non-zero values to the left, zeros to the right
    private int[] slide(int[] line) {
        int[] result = new int[gridSize + 1];
        int insert = 0;
        for (int j = 0; j < gridSize; j++) {
            if (line[j] != 0) result[insert++] = line[j];
        }
        return result;
    }

    // Merge adjacent equal values left-to-right
    // Stores score in result[gridSize]
    private int[] merge(int[] line) {
        int[] result = new int[gridSize + 1];
        int score = 0;
        int j = 0, insert = 0;
        while (j < gridSize) {
            if (line[j] != 0 && j + 1 < gridSize && line[j] == line[j + 1]) {
                int merged = line[j] * 2;
                result[insert++] = merged;
                score += merged;
                j += 2; // skip both merged cells
            } else {
                result[insert++] = line[j];
                j++;
            }
        }
        result[gridSize] = score; // carry score out
        return result;
    }

    // Spawn a random tile (2 or 4) in a random empty cell for TESTING
    // Returns false if no empty cells available (game over)
    public boolean spawnRandom() {
        ArrayList<int[]> empty = new ArrayList<>();
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                if (getCellVal(i, j) == 0)
                    empty.add(new int[]{i, j});

        if (empty.isEmpty()) return false;

        int[] pos = empty.get((int)(Math.random() * empty.size()));
        int val = Math.random() < 0.9 ? 2 : 4; // 90% chance of 2, 10% of 4
        setCellVal(pos[0], pos[1], val);
        return true;
    }

    public void updateScore(int scoreToAdd){
        previousScore = currentScore;
        currentScore += scoreToAdd;
    }

    // return 0 if nothing, 1 if win(score reaches stopScore),
    // -1 if lose (no more moves)
    public int checkEndGame(){
        // Win
        for (int i =0; i < gridSize; i++)
            for(int j = 0; j < gridSize; j++)
                if (getCellVal(i, j) == this.stopScore)
                    return 1;

        // Lose
        if (!hasMoveLeft())
            return -1;

        return 0;
    }

    public boolean hasMoveLeft(){
        for (int i = 0; i < gridSize; i++){
            for (int j = 0; j < gridSize; j++){
                if (getCellVal(i, j) == 0)
                    return true;

                if(j+ 1 < gridSize && getCellVal(i, j) == getCellVal(i, j+1))
                    return true;

                if(i+1 < gridSize && getCellVal(i, j) == getCellVal(i+1, j))
                    return true;
            }
        }
        return false;
    }


    private boolean boardChanged() {
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                if (cells.get(i).get(j).getVal() != previousCells.get(i).get(j).getVal())
                    return true;
        return false;
    }
    /// /////////////////// EXTRA ///////////////////
    // Call this before every move to snapshot the state
    private void saveState() {
        previousCells = new ArrayList<>();
        for (ArrayList<Cell> row : cells) {
            ArrayList<Cell> rowCopy = new ArrayList<>();
            for (Cell cell : row) {
                rowCopy.add(new Cell(cell.getVal()));
            }
            previousCells.add(rowCopy);
        }
        previousScore = currentScore;
    }

    // Restore the snapshot
    public int undo() {
        if (previousCells == null) return currentScore; // nothing to undo
        cells = previousCells;
        previousCells = null; // clear so undo can't be called twice in a row
        currentScore = previousScore;
        return currentScore;
    }

    // Wipe the board and start fresh
    public void newGame() {
        previousCells = null;
        previousScore = 0;
        currentScore = 0;
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                cells.get(i).get(j).setVal(0);
        spawnRandom();
        spawnRandom();
    }

    public boolean canUndo() {
        return previousCells != null;
    }

    public int getScore(){
        return this.currentScore;
    }

    public int getMoves(){
        return this.moves;
    }
}

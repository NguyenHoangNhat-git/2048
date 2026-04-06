package com.example.a2048;

public class SavedGameState {
    public final int   gridSize;
    public final int[] board;
    public final int   score;
    public final int   moves;

    public SavedGameState(int gridSize, int[] board, int score, int moves) {
        this.gridSize = gridSize;
        this.board    = board;
        this.score    = score;
        this.moves    = moves;
    }
}
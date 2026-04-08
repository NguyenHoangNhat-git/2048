package com.example.a2048;

public class TutorialStep {
    public final String instruction;
    public final String hint;
    public final int[][]  board;        // preset board for this step
    public final int      expectedDir;  // 0=any, 1=left, 2=right, 3=up, 4=down

    public static final int ANY   = 0;
    public static final int LEFT  = 1;
    public static final int RIGHT = 2;
    public static final int UP    = 3;
    public static final int DOWN  = 4;

    public TutorialStep(String instruction, String hint, int[][] board, int expectedDir) {
        this.instruction = instruction;
        this.hint        = hint;
        this.board       = board;
        this.expectedDir = expectedDir;
    }
}
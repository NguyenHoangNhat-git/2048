package com.example.a2048;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.a2048.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Game game;
    private GameGrid gameGrid;
    private DatabaseHandler db;
    private long gameStartTime;

    private boolean isSoundOn = true;
    private SoundManager soundManager;


    /// //////////// SETUP ////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHandler(this);

        game = new Game(4);
        game.spawnRandom();
        game.spawnRandom();

        gameGrid = new GameGrid(binding.gameGrid, this, game);
        gameGrid.build();
        setupTestScenarios();

        binding.soundBtn.setOnClickListener(v -> toggleSound());

        setUpBtn();
        updateScore(0);
        binding.maxScore.setText(Integer.toString(db.getHighestScore()));

        soundManager = new SoundManager(this);

        /// //////////// SETUP ////////////////////////////
        binding.gameGrid.setOnTouchListener(new SwipeHandler(this, new SwipeHandler.SwipeListener() {
            @Override
            public void onSwipeLeft() {
                int gained = game.moveLeft();
                update(gained);
            }

            @Override
            public void onSwipeRight() {
                int gained = game.moveRight();
                update(gained);
            }

            @Override
            public void onSwipeUp() {
                int gained = game.moveUp();
                update(gained);
            }

            @Override
            public void onSwipeDown() {
                int gained = game.moveDown();
                update(gained);
            }
        }));

        gameStartTime = System.currentTimeMillis();
    }

    public void setUpBtn(){
        binding.newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.newGame();
                gameGrid.refresh();
                updateScore(0);
            }
        });
        binding.undoBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int score = game.undo();
                gameGrid.refresh();
                updateScore(0);
            }
        });
        binding.viewStatsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                viewStats();
            }
        });
        binding.menuBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToMenu();
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();
    }
    /// /////////////// VIEW STATS ///////////////////////////
    private void viewStats() {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }
    /// /////////////// GO TO MENU ////////////////////////
    private void goToMenu() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
    //////////////////// END GAME //////////////////
    private void handleEndGame(int state) {
        int elapsedSeconds = (int)((System.currentTimeMillis() - gameStartTime) / 1000);
        game.checkMilestones();
        db.saveGameResult(
                game.getScore(),
                game.getMoves(),
                elapsedSeconds,
                game.hasReached512(),
                game.hasReached1024()
        );

        int gridSize = game.getGridSize();

        // Flatten board to int[]
        int[] board = new int[gridSize * gridSize];
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                board[i * gridSize + j] = game.getCellVal(i, j);

        Intent intent = new Intent(this, EndGameActivity.class);
        intent.putExtra("if_win",   state == 1);
        intent.putExtra("score",    game.getScore());
        intent.putExtra("moves",    game.getMoves());
        intent.putExtra("grid_size", gridSize);
        intent.putExtra("board",    board);
        startActivity(intent);
        
        finish(); // remove MainActivity from back stack so back button doesn't return to a dead game
    }

    /// /////////////// SOUND /////////////////////
    private void toggleSound() {
        isSoundOn = !isSoundOn;
        soundManager.setEnabled(isSoundOn);
        binding.soundBtn.setCompoundDrawablesWithIntrinsicBounds(
                isSoundOn ? R.drawable.ic_fas_volume_up
                        : R.drawable.ic_fas_volume_off,
                0, 0, 0
        );
        binding.soundBtn.setText(isSoundOn ? "Sound" : "Muted");
    }
    /// /////////////// UTILITY ////////////////////////

    public void update(int gained){
        if (gained != -1) {
            game.spawnRandom();
            game.updateScore(gained);
        }

        if (gained > 0) soundManager.playMerge();
        else soundManager.playSwipe();

        gameGrid.refresh();
        updateScore(game.getScore());

        int state = game.checkEndGame();

        if (state != 0) {
            if (state == 1) soundManager.playWon();
            else soundManager.playGameOver();
            handleEndGame(state);
        }
    }

    public void updateScore(int score){
        binding.score.setText(Integer.toString(game.getScore()));
    }


    /// /////////////////// TESTING ///////////////////////
    private void setupTestScenarios() {
        binding.getRoot().setFocusableInTouchMode(true);
        binding.getRoot().requestFocus();
        binding.getRoot().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            switch (keyCode) {
                case KeyEvent.KEYCODE_Q: loadScenario2048(); return true;
                case KeyEvent.KEYCODE_W: loadScenario1024(); return true;
                case KeyEvent.KEYCODE_E: loadScenario512();  return true;
                case KeyEvent.KEYCODE_R: loadScenarioLose(); return true;
            }
            return false;
        });
    }

    // Q — one move away from 2048
    private void loadScenario2048() {
        int[][] board = {
                {1024, 1024,   0,   0},
                { 512,  256, 128,  64},
                {  32,   16,   8,   4},
                {   2,   2,    0,   0}
        };
        loadBoard(board, 15000);
    }

    // W — one move away from 1024
    private void loadScenario1024() {
        int[][] board = {
                { 512,  512,   0,   0},
                { 256,  128,  64,  32},
                {  16,    8,   4,   2},
                {   2,    0,   0,   0}
        };
        loadBoard(board, 7000);
    }

    // E — one move away from 512
    private void loadScenario512() {
        int[][] board = {
                { 256,  256,   0,   0},
                { 128,   64,  32,  16},
                {   8,    4,   2,   0},
                {   0,    0,   0,   0}
        };
        loadBoard(board, 3000);
    }
    // R — one move away from losing (board full, no adjacent equal tiles)
    private void loadScenarioLose() {
        int[][] board = {
                {   2, 256,   4, 128},
                { 512,   8, 512,   2},
                {   4, 128,   2, 256},
                { 128,   2, 256,   4}
        };
        loadBoard(board, 500);
        Toast.makeText(this, "Test scenario loaded — any swipe triggers game over!", Toast.LENGTH_SHORT).show();
    }

    private void loadBoard(int[][] board, int score) {
        game.newGame(); // resets the board and score
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                game.setCellVal(i, j, board[i][j]);

        // Manually set score since newGame() resets it
        game.updateScore(score - game.getScore());

        gameGrid.refresh();
        Toast.makeText(this, "Test scenario loaded — swipe left to trigger!", Toast.LENGTH_SHORT).show();
    }
}
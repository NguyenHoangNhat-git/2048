package com.example.a2048;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.a2048.databinding.ActivityMainBinding;

public class MainActivity extends BaseGameActivity {
    private ActivityMainBinding binding;
    private Game game;
    private GameGrid gameGrid;
    private DatabaseHandler db;
    private long gameStartTime;

    private int currentGridSize = 4;

    /// //////////// SETUP ////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHandler(this);

        boolean forceNew = getIntent().getBooleanExtra("force_new_game", false);
        int gridSize = getSharedPreferences(GridSizeActivity.PREFS_NAME, MODE_PRIVATE)
                .getInt(GridSizeActivity.KEY_GRID_SIZE, 4);


        SavedGameState saved = forceNew ? null : db.loadGameState();
        if (saved != null) {
            game = new Game(saved.gridSize);
            for (int i = 0; i < saved.gridSize; i++)
                for (int j = 0; j < saved.gridSize; j++)
                    game.setCellVal(i, j, saved.board[i * saved.gridSize + j]);
            game.updateScore(saved.score);
            game.setMoves(saved.moves); // add setMoves() to Game.java — see below
        } else {
            if (forceNew) db.clearGameState();
            game = new Game(currentGridSize);
            game.spawnRandom();
            game.spawnRandom();
        }

        gameGrid = new GameGrid(binding.gameGrid, this, game);
        gameGrid.build();
        setupTestScenarios();


        setUpBtn();
        updateScore(0);
        binding.maxScore.setText(Integer.toString(db.getHighestScore()));

        soundManager = new SoundManager(this);
        setupToggles();

        boolean isCurrentlyDark = AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES;

        /// //////////// SETUP ////////////////////////////
        binding.gameGrid.setOnTouchListener(new SwipeHandler(this, new SwipeHandler.SwipeListener() {
            @Override public void onSwipeLeft()  { handleMove(game.moveLeft());  }
            @Override public void onSwipeRight() { handleMove(game.moveRight()); }
            @Override public void onSwipeUp()    { handleMove(game.moveUp());    }
            @Override public void onSwipeDown()  { handleMove(game.moveDown());  }
        }));

        gameStartTime = System.currentTimeMillis();
    }


    public void setUpBtn(){
        binding.newBtn.setOnClickListener(v -> {
            game.newGame();
            gameGrid.refresh();
            updateScore(0);
        });

        binding.undoBtn.setOnClickListener(v -> {
            if (!game.canUndo()) return;
            game.undo();
            gameGrid.refresh();
            updateScore(0);
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
        binding.animationBtn.setOnClickListener(v -> toggleAnimation());
    }

    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();
    }

    // Save state when app goes to background
    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentGameState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        int savedSize = getSharedPreferences(GridSizeActivity.PREFS_NAME, MODE_PRIVATE)
                .getInt(GridSizeActivity.KEY_GRID_SIZE, 4);

        if (savedSize != currentGridSize) {
            currentGridSize = savedSize;
            // Clear any saved state so it doesn't restore the old grid size
            db.clearGameState();
            game = new Game(currentGridSize);
            game.spawnRandom();
            game.spawnRandom();
            gameGrid = new GameGrid(binding.gameGrid, this, game);
            gameGrid.build();
        }
    }

    /// //////////////////// BASE GAME /////////////////////////////////
    @Override
    protected android.widget.Button getSoundBtn()     { return binding.soundBtn;     }
    @Override
    protected android.widget.Button getDarkModeBtn()  { return binding.darkModeBtn;  }
    @Override
    protected android.widget.Button getAnimationBtn() { return binding.animationBtn; }
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
        db.clearGameState();

        int elapsedSeconds = (int)((System.currentTimeMillis() - gameStartTime) / 1000);
        game.checkMilestones();
        db.saveGameResult(
                game.getScore(),
                game.getMoves(),
                elapsedSeconds,
                game.hasReached512(),
                game.hasReached1024()
        );
        db.insertScore(game.getScore());

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
    /// /////////////// UTILITY ////////////////////////

    private void handleMove(int gained) {
        if (gained == -1) return;

        game.spawnRandom();
        game.updateScore(gained);
        gameGrid.refresh();
        updateScore(game.getScore());

        if (gained > 0) {
            soundManager.playMerge();
            if (isAnimationOn) gameGrid.animateMerge();
        } else {
            soundManager.playSwipe();
        }

        int state = game.checkEndGame();
        if (state != 0) {
            if (state == 1) soundManager.playWon();
            else            soundManager.playGameOver();
            handleEndGame(state);
        }
    }

    public void updateScore(int score){
        binding.score.setText(Integer.toString(game.getScore()));
    }

    private void saveCurrentGameState() {
        int gridSize = game.getGridSize();
        int[] board  = new int[gridSize * gridSize];
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                board[i * gridSize + j] = game.getCellVal(i, j);
        db.saveGameState(gridSize, board, game.getScore(), game.getMoves());
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
    private boolean checkGridSize() {
        if (game.getGridSize() != 4) return false;
        return true;
    }

    // Q — one move away from 2048
    private void loadScenario2048() {
        if (!checkGridSize()) return;
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
        if (!checkGridSize()) return;
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
        if (!checkGridSize()) return;
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
        if (!checkGridSize()) return;
        int[][] board = {
                {   2, 256,   4, 128},
                { 512,   8, 512,   0},
                {   4, 128,   2, 256},
                { 128,   2, 256,   4}
        };
        loadBoard(board, 500);
        Toast.makeText(this, "Test scenario loaded — swipe down = game over!", Toast.LENGTH_SHORT).show();
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

    private int[][] captureBoard() {
        int gridSize = game.getGridSize();
        int[][] board = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                board[i][j] = game.getCellVal(i, j);
        return board;
    }


}
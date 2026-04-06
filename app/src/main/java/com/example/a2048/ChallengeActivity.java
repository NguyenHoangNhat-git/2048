package com.example.a2048;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a2048.databinding.ActivityChallengeBinding;

public class ChallengeActivity extends AppCompatActivity {

    private ActivityChallengeBinding binding;
    private Game game;
    private GameGrid gameGrid;
    private SoundManager soundManager;
    private CountDownTimer countDownTimer;
    private SwipeHandler swipeHandler;

    private int targetScore;
    private long timeLimitMs;
    private long millisRemaining;
    private boolean gameEnded = false;

    // Target options and their time limits in milliseconds
    private static final int[]  TARGETS   = {256, 512, 1024};
    private static final long[] TIME_LIMITS = {
            2 * 60 * 1000L,  // 256 → 2 minutes
            4 * 60 * 1000L,  // 512 → 4 minutes
            7 * 60 * 1000L   // 1024 → 7 minutes
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChallengeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        soundManager = new SoundManager(this);

        setupChallenge();

        binding.challengeNewBtn.setOnClickListener(v -> {
            cancelTimer();
            setupChallenge();
        });

        binding.challengeTitle.setOnClickListener(v -> finish());
        binding.challengeBackBtn.setOnClickListener(v -> confirmQuit());
    }

    // Pick a random target and time, build a fresh board
    private void setupChallenge() {
        gameEnded = false;

        int pick = (int)(Math.random() * TARGETS.length);
        targetScore = TARGETS[pick];
        timeLimitMs = TIME_LIMITS[pick];

        binding.challengeTarget.setText(String.valueOf(targetScore));
        binding.challengeScore.setText("0");

        game = new Game(4);
        game.setChallengeTarget(targetScore); // see Game.java addition below
        game.spawnRandom();
        game.spawnRandom();

        gameGrid = new GameGrid(binding.challengeGameGrid, this, game);
        gameGrid.build();

        setupSwipe();
        startCountdown(timeLimitMs);
    }

    private void startCountdown(long ms) {
        cancelTimer();
        countDownTimer = new CountDownTimer(ms, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished;
                long secs = millisUntilFinished / 1000;
                binding.countdownTimer.setText(
                        (secs / 60) + ":" + String.format("%02d", secs % 60));
                // Turn timer red in last 30 seconds
                if (secs <= 30)
                    binding.countdownTimer.setTextColor(
                            getColor(android.R.color.holo_red_light));
                else
                    binding.countdownTimer.setTextColor(
                            getColor(R.color.white));
            }

            @Override
            public void onFinish() {
                binding.countdownTimer.setText("0:00");
                endChallenge(false); // time ran out
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void setupSwipe() {
        swipeHandler = new SwipeHandler(this, new SwipeHandler.SwipeListener() {
            @Override public void onSwipeLeft()  { handleMove(game.moveLeft());  }
            @Override public void onSwipeRight() { handleMove(game.moveRight()); }
            @Override public void onSwipeUp()    { handleMove(game.moveUp());    }
            @Override public void onSwipeDown()  { handleMove(game.moveDown());  }
        });
        binding.challengeGameGrid.setOnTouchListener(swipeHandler);
    }

    private void handleMove(int score) {
        if (gameEnded || score == -1) return;

        if (score > 0) soundManager.playMerge();
        else           soundManager.playSwipe();

        game.updateScore(score);
        game.spawnRandom();
        gameGrid.refresh();

        binding.challengeScore.setText(String.valueOf(game.getScore()));

        // Check win
        if (game.getScore() >= targetScore) {
            endChallenge(true);
            return;
        }

        // Check lose
        if (game.checkEndGame() == -1) endChallenge(false);
    }

    private void endChallenge(boolean won) {
        if (gameEnded) return;
        gameEnded = true;
        cancelTimer();

        if (won) soundManager.playWon();
        else     soundManager.playGameOver();

        // Pass result to EndGameActivity
        int gridSize = game.getGridSize();
        int[] board  = new int[gridSize * gridSize];
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                board[i * gridSize + j] = game.getCellVal(i, j);

        Intent intent = new Intent(this, EndGameActivity.class);
        intent.putExtra("if_win",    won);
        intent.putExtra("score",     game.getScore());
        intent.putExtra("moves",     game.getMoves());
        intent.putExtra("grid_size", gridSize);
        intent.putExtra("board",     board);
        intent.putExtra("is_challenge", true);
        intent.putExtra("target",    targetScore);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause()  {
        super.onPause();
        cancelTimer(); }
    @Override
    protected void onResume() {
        super.onResume();
        if (!gameEnded)
            startCountdown(millisRemaining > 0 ? millisRemaining : timeLimitMs);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        soundManager.release();
        cancelTimer();
    }

    private void confirmQuit() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Quit Challenge?")
                .setMessage("Your current challenge progress will be lost.")
                .setPositiveButton("Quit", (dialog, which) -> {
                    cancelTimer();
                    finish(); // returns to MenuActivity
                })
                .setNegativeButton("Keep Playing", null)
                .show();
    }
}
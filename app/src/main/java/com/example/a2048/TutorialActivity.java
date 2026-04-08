package com.example.a2048;

import android.content.Intent;
import android.os.Bundle;
import com.example.a2048.databinding.ActivityTutorialBinding;
import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends BaseGameActivity {

    private ActivityTutorialBinding binding;
    private Game game;
    private GameGrid gameGrid;

    private List<TutorialStep> steps;
    private int currentStep = 0;

    @Override
    protected android.widget.Button getSoundBtn()     { return binding.soundBtn;     }
    @Override
    protected android.widget.Button getDarkModeBtn()  { return binding.darkModeBtn;  }
    @Override
    protected android.widget.Button getAnimationBtn() { return binding.animationBtn; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTutorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        soundManager = new SoundManager(this);
        setupToggles();
        buildSteps();

        binding.tutorialBackBtn.setOnClickListener(v -> confirmQuit());
        binding.tutorialRestartBtn.setOnClickListener(v -> {
            currentStep = 0;
            loadStep(currentStep);
        });
        binding.tutorialSkipBtn.setOnClickListener(v -> {
            // Skip to next step without needing the correct swipe
            advanceStep();
        });

        game = new Game(4);
        gameGrid = new GameGrid(binding.tutorialGameGrid, this, game);
        gameGrid.build();

        // Load first step after grid is built
        binding.tutorialGameGrid.post(() -> loadStep(0));

        setupSwipe();
    }

    private void buildSteps() {
        steps = new ArrayList<>();

        steps.add(new TutorialStep(
                "Welcome to 2048! Swipe to slide all tiles in that direction.",
                "Swipe LEFT to get started",
                new int[][]{
                        {0, 0, 2, 2},
                        {0, 0, 0, 0},
                        {0, 0, 0, 0},
                        {0, 0, 0, 0}
                },
                TutorialStep.LEFT
        ));

        steps.add(new TutorialStep(
                "When two tiles with the same number collide, they merge into one!",
                "Swipe LEFT to merge the 4s",
                new int[][]{
                        {0, 0, 4, 4},
                        {0, 0, 2, 2},
                        {0, 0, 0, 0},
                        {0, 0, 0, 0}
                },
                TutorialStep.LEFT
        ));

        steps.add(new TutorialStep(
                "You can also merge vertically. Tiles slide in the direction you swipe.",
                "Swipe UP to merge",
                new int[][]{
                        {0, 2, 0, 0},
                        {0, 2, 0, 0},
                        {0, 0, 0, 0},
                        {0, 0, 0, 0}
                },
                TutorialStep.UP
        ));

        steps.add(new TutorialStep(
                "Plan ahead! A new tile appears after every move. Don't fill the board!",
                "Swipe any direction",
                new int[][]{
                        {2,  4,  2,  4},
                        {4,  2,  4,  2},
                        {2,  4,  2,  0},
                        {4,  2,  0,  0}
                },
                TutorialStep.ANY
        ));

        steps.add(new TutorialStep(
                "Great job! Keep merging to reach 2048. Good luck!",
                "Swipe any direction to finish",
                new int[][]{
                        {128, 64,  32, 16},
                        {  8,  4,   2,  0},
                        {  0,  0,   0,  0},
                        {  0,  0,   0,  0}
                },
                TutorialStep.ANY
        ));
    }

    private void loadStep(int index) {
        TutorialStep step = steps.get(index);

        // Reset game with preset board
        game.newGame();
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                game.setCellVal(i, j, step.board[i][j]);

        gameGrid.refresh();
        binding.tutorialScore.setText("0");
        binding.tutorialStep.setText((index + 1) + "/" + steps.size());
        binding.tutorialInstruction.setText(step.instruction);
        binding.tutorialHint.setText(step.hint);
    }

    private void setupSwipe() {
        binding.tutorialGameGrid.setOnTouchListener(new SwipeHandler(this,
                new SwipeHandler.SwipeListener() {
                    @Override public void onSwipeLeft()  { handleSwipe(TutorialStep.LEFT);  }
                    @Override public void onSwipeRight() { handleSwipe(TutorialStep.RIGHT); }
                    @Override public void onSwipeUp()    { handleSwipe(TutorialStep.UP);    }
                    @Override public void onSwipeDown()  { handleSwipe(TutorialStep.DOWN);  }
                }));
    }

    private void handleSwipe(int dir) {
        TutorialStep step = steps.get(currentStep);

        // If step expects a specific direction, reject wrong swipes
        if (step.expectedDir != TutorialStep.ANY && step.expectedDir != dir) {
            binding.tutorialHint.setText("❌ Try the suggested direction!");
            return;
        }

        int gained;
        switch (dir) {
            case TutorialStep.LEFT:  gained = game.moveLeft();  break;
            case TutorialStep.RIGHT: gained = game.moveRight(); break;
            case TutorialStep.UP:    gained = game.moveUp();    break;
            case TutorialStep.DOWN:  gained = game.moveDown();  break;
            default: return;
        }

        if (gained == -1) {
            binding.tutorialHint.setText("❌ No tiles moved! Try again.");
            return;
        }

        game.updateScore(gained);
        game.spawnRandom();
        gameGrid.refresh();
        binding.tutorialScore.setText(String.valueOf(game.getScore()));

        if (gained > 0) {
            soundManager.playMerge();
            if (isAnimationOn) gameGrid.animateMerge();
        } else {
            soundManager.playSwipe();
        }

        // Short delay before advancing so user sees the result
        binding.tutorialGameGrid.postDelayed(() -> advanceStep(), 600);
    }

    private void advanceStep() {
        currentStep++;
        if (currentStep >= steps.size()) {
            // Tutorial complete — go back to menu
            Intent intent = new Intent(this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            loadStep(currentStep);
        }
    }

    private void confirmQuit() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Quit Tutorial?")
                .setPositiveButton("Quit", (dialog, which) -> finish())
                .setNegativeButton("Keep Playing", null)
                .show();
    }

    @Override
    public void onBackPressed() { confirmQuit(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();
    }
}
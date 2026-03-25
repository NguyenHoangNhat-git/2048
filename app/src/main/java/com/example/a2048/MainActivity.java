package com.example.a2048;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.example.a2048.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Game game;
    private GameGrid gameGrid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        game = new Game(4);
        game.spawnRandom();
        game.spawnRandom();

        gameGrid = new GameGrid(binding.gameGrid, this, game);
        gameGrid.build();

        setUpBtn();
        updateScore(0);

        binding.gameGrid.setOnTouchListener(new SwipeHandler(this, new SwipeHandler.SwipeListener() {
            @Override
            public void onSwipeLeft() {
                int score = game.moveLeft();
                game.spawnRandom();
                gameGrid.refresh();
                updateScore(score);
            }

            @Override
            public void onSwipeRight() {
                int score = game.moveRight();
                game.spawnRandom();
                gameGrid.refresh();
                updateScore(score);
            }

            @Override
            public void onSwipeUp() {
                int score = game.moveUp();
                game.spawnRandom();
                gameGrid.refresh();
                updateScore(score);
            }

            @Override
            public void onSwipeDown() {
                int score = game.moveDown();
                game.spawnRandom();
                gameGrid.refresh();
                updateScore(score);
            }
        }));
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
    }
    public void updateScore(int score){
        int newScore = game.getScore() + score;
        game.updateScore(newScore);
        binding.score.setText(Integer.toString(game.getScore()));
    }
}
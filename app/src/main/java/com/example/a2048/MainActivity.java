package com.example.a2048;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.example.a2048.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Game game;
    private GameGrid gameGrid;


    /// //////////// SETUP ////////////////////////////
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

    //////////////////// END GAME //////////////////
    private void handleEndGame(int state) {
        if (state == 1) {
            // navigate to win screen
        } else if (state == -1) {
            // navigate to lose screen
            binding.title.setText("LOST");
        }
    }

    /// /////////////// UTILITY ////////////////////////

    public void update(int gained){
        if (gained != -1) {
            game.spawnRandom();
            game.updateScore(gained);
        }

        gameGrid.refresh();
        updateScore(game.getScore());

        int state = game.checkEndGame();
        if (state != 0) handleEndGame(state);
    }

    public void updateScore(int score){
        binding.score.setText(Integer.toString(game.getScore()));
    }



}
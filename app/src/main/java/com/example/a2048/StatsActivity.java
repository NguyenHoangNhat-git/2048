package com.example.a2048;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a2048.databinding.ActivityStatsBinding;

public class StatsActivity extends AppCompatActivity {
    private ActivityStatsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DatabaseHandler db = new DatabaseHandler(this);

        binding.bestScore.setText(String.valueOf(db.getHighestScore()));
        binding.totalScore.setText(String.valueOf(db.getTotalScore()));
        binding.totalGames.setText(String.valueOf(db.getTotalGames()));

        binding.games512.setText(String.valueOf(db.getGames512()));
        binding.shortestTime512.setText(DatabaseHandler.formatTime(db.getShortestTime512()));
        binding.fewestMoves512.setText(String.valueOf(db.getFewestMoves512() == 0 ? "—" : db.getFewestMoves512()));

        binding.games1024.setText(String.valueOf(db.getGames1024()));
        binding.shortestTime1024.setText(DatabaseHandler.formatTime(db.getShortestTime1024()));
        binding.fewestMoves1024.setText(String.valueOf(db.getFewestMoves1024() == 0 ? "—" : db.getFewestMoves1024()));


        binding.backBtn.setOnClickListener(v-> finish());
    }
}
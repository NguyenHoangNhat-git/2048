package com.example.a2048;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a2048.databinding.ActivityStatsBinding;

import java.util.List;

public class StatsActivity extends AppCompatActivity {
    private ActivityStatsBinding binding;

    DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHandler(this);

        // Best stats
        binding.bestScore.setText(String.valueOf(db.getHighestScore()));
        binding.totalScore.setText(String.valueOf(db.getTotalScore()));
        binding.totalGames.setText(String.valueOf(db.getTotalGames()));

        binding.games512.setText(String.valueOf(db.getGames512()));
        binding.shortestTime512.setText(DatabaseHandler.formatTime(db.getShortestTime512()));
        binding.fewestMoves512.setText(String.valueOf(db.getFewestMoves512() == 0 ? "—" : db.getFewestMoves512()));

        binding.games1024.setText(String.valueOf(db.getGames1024()));
        binding.shortestTime1024.setText(DatabaseHandler.formatTime(db.getShortestTime1024()));
        binding.fewestMoves1024.setText(String.valueOf(db.getFewestMoves1024() == 0 ? "—" : db.getFewestMoves1024()));


        // All scores
        List<Integer> scores = db.getAllScores();
        for (int rank = 0; rank < scores.size(); rank++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView rankView = new TextView(this);
            rankView.setText("#" + (rank + 1));
            rankView.setTextSize(18);
            rankView.setTextColor(getColor(R.color.stats_blur_color));
            rankView.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));

            TextView scoreView = new TextView(this);
            scoreView.setText(String.valueOf(scores.get(rank)));
            scoreView.setTextSize(18);
            scoreView.setTextColor(getColor(R.color.dark_text_color));
            scoreView.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            row.addView(rankView);
            row.addView(scoreView);
            binding.scoresContainer.addView(row);
        }

        // Reset button
        binding.statsResetBtn.setOnClickListener(v -> {
            db.resetStats();
            binding.scoresContainer.removeAllViews();
            binding.bestScore.setText("0");
            binding.totalScore.setText("0");
            binding.totalGames.setText("0");
            binding.games512.setText("0");
            binding.shortestTime512.setText("—");
            binding.fewestMoves512.setText("—");
            binding.games1024.setText("0");
            binding.shortestTime1024.setText("—");
            binding.fewestMoves1024.setText("—");
            Toast.makeText(this, "Stats reset", Toast.LENGTH_SHORT).show();
        });
        binding.backBtn.setOnClickListener(v-> finish());
    }
}
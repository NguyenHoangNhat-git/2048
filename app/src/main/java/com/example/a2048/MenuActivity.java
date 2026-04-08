package com.example.a2048;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a2048.databinding.ActivityMenuBinding;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;


    private final ActivityResultLauncher<Intent> gridSizeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean changed = result.getData().getBooleanExtra("size_changed", false);
                    if (changed) {
                        // Force MainActivity to restart fresh
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("force_new_game", true);
                        startActivity(intent);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(v-> finish());
        binding.viewStatsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(binding.getRoot().getContext(), StatsActivity.class);
                startActivity(intent);
            }
        });
        binding.challengeMode.setOnClickListener(v -> {
            startActivity(new Intent(this, ChallengeActivity.class));
        });
        binding.tutorialMode.setOnClickListener(v ->
                startActivity(new Intent(this, TutorialActivity.class)));

        /// //////////////////// CHANGE GRID SIZE ////////////////////
        binding.classicMode.setOnClickListener(v ->
                gridSizeLauncher.launch(new Intent(this, GridSizeActivity.class)));
    }
}
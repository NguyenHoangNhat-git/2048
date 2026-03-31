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


        binding.backBtn.setOnClickListener(v-> finish());
    }
}
package com.example.a2048;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a2048.databinding.ActivityGridSizeBinding;

public class GridSizeActivity extends AppCompatActivity {

    private ActivityGridSizeBinding binding;
    public static final String PREFS_NAME = "game_prefs";
    public static final String KEY_GRID_SIZE = "grid_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGridSizeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Highlight the currently selected size
        int current = getSavedGridSize();
        highlightCurrent(current);

        binding.backBtn.setOnClickListener(v -> finish());

        binding.grid3x3.setOnClickListener(v -> selectSize(3));
        binding.grid4x4.setOnClickListener(v -> selectSize(4));
        binding.grid5x5.setOnClickListener(v -> selectSize(5));
        binding.grid6x6.setOnClickListener(v -> selectSize(6));
    }

    private void selectSize(int size) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int previousSize = prefs.getInt(KEY_GRID_SIZE, 4);
        prefs.edit().putInt(KEY_GRID_SIZE, size).apply();
        highlightCurrent(size);

        // Send result back so MainActivity knows if size changed
        Intent result = new Intent();
        result.putExtra("size_changed", size != previousSize);
        setResult(RESULT_OK, result);

        android.widget.Toast.makeText(this,
                "Grid size set to " + size + "x" + size,
                android.widget.Toast.LENGTH_SHORT).show();
        finish();
    }

    private void highlightCurrent(int size) {
        int defaultTint  = getColor(R.color.title_color);
        int selectedTint = getColor(R.color.undo_btn_color);

        setButtonTint(binding.grid3x3, size == 3 ? selectedTint : defaultTint);
        setButtonTint(binding.grid4x4, size == 4 ? selectedTint : defaultTint);
        setButtonTint(binding.grid5x5, size == 5 ? selectedTint : defaultTint);
        setButtonTint(binding.grid6x6, size == 6 ? selectedTint : defaultTint);
    }
    private void setButtonTint(androidx.appcompat.widget.AppCompatButton btn, int color) {
        btn.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(color)
        );
    }

    private int getSavedGridSize() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_GRID_SIZE, 4); // default 4x4
    }
}
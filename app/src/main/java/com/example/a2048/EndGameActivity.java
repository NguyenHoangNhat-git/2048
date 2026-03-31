package com.example.a2048;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.a2048.databinding.ActivityEndGameBinding;

import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EndGameActivity extends AppCompatActivity {

    private ActivityEndGameBinding binding;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEndGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Read extras from MainActivity
        boolean ifWin  = getIntent().getBooleanExtra("if_win", false);
        score = getIntent().getIntExtra("score", 0);
        int moves      = getIntent().getIntExtra("moves", 0);
        int gridSize   = getIntent().getIntExtra("grid_size", 4);
        int[] board    = getIntent().getIntArrayExtra("board");

        // Set title and stats
        binding.endTitle.setText(ifWin ? "YOU WIN!" : "GAME OVER");
        binding.endStats.setText("Score: " + score + " — Moves: " + moves);

        // Rebuild the board as read-only snapshot
        if (board != null) {
            Game frozenGame = new Game(gridSize);
            for (int i = 0; i < gridSize; i++)
                for (int j = 0; j < gridSize; j++)
                    frozenGame.setCellVal(i, j, board[i * gridSize + j]);

            GameGrid gameGrid = new GameGrid(binding.gameGrid, this, frozenGame);
            gameGrid.build(); // renders the last board state, no interaction
        }

        // New game — restart MainActivity fresh
        binding.newGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // In onCreate, after existing button setup:
        binding.shareBtn.setOnClickListener(v -> shareScreenshot());
        binding.downloadBtn.setOnClickListener(v -> saveScreenshot());
    }


    // Capture the entire screen as a bitmap
    private Bitmap captureScreen() {
        View root = binding.getRoot();
        Bitmap bitmap = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        root.draw(canvas);
        return bitmap;
    }

    // Save bitmap to a temp file and return its shareable URI
    private Uri bitmapToUri(Bitmap bitmap) throws IOException {
        File cachedir = new File(getCacheDir(), "screenshots");
        cachedir.mkdirs();
        File file = new File(cachedir, "2048_result.png");
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
        // FileProvider is needed to share files safely from app cache
        return FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
    }

    private void shareScreenshot() {
        try {
            Uri uri = bitmapToUri(captureScreen());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, "I just scored " + this.score + " in 2048!");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share via"));
        } catch (IOException e) {
            Toast.makeText(this, "Failed to share screenshot", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveScreenshot() {
        try {
            Bitmap bitmap = captureScreen();
            String filename = "2048_" + System.currentTimeMillis() + ".png";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/2048");

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            Toast.makeText(this, "Saved to Pictures/2048", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
        }
    }
}
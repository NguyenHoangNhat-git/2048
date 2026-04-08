package com.example.a2048;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class BaseGameActivity extends AppCompatActivity {

    protected SoundManager soundManager;
    protected boolean isSoundOn      = true;
    protected boolean isAnimationOn  = true;

    // Each activity provides its own button bindings
    protected abstract android.widget.Button getSoundBtn();
    protected abstract android.widget.Button getDarkModeBtn();
    protected abstract android.widget.Button getAnimationBtn();

    protected void setupToggles() {
        // Sync dark mode button text to current state on launch
        boolean isCurrentlyDark = AppCompatDelegate.getDefaultNightMode()
                == AppCompatDelegate.MODE_NIGHT_YES;
        getDarkModeBtn().setText(isCurrentlyDark ? "Light Mode" : "Dark Mode");

        getSoundBtn().setOnClickListener(v     -> toggleSound());
        getDarkModeBtn().setOnClickListener(v  -> toggleDarkMode());
        getAnimationBtn().setOnClickListener(v -> toggleAnimation());
    }

    protected void toggleSound() {
        isSoundOn = !isSoundOn;
        soundManager.setEnabled(isSoundOn);
        getSoundBtn().setCompoundDrawablesWithIntrinsicBounds(
                isSoundOn ? R.drawable.ic_fas_volume_up
                        : R.drawable.ic_fas_volume_off,
                0, 0, 0
        );
        getSoundBtn().setText(isSoundOn ? "Sound" : "Muted");
    }

    protected void toggleDarkMode() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        boolean isCurrentlyDark = currentMode == AppCompatDelegate.MODE_NIGHT_YES;
        AppCompatDelegate.setDefaultNightMode(
                isCurrentlyDark ? AppCompatDelegate.MODE_NIGHT_NO
                        : AppCompatDelegate.MODE_NIGHT_YES
        );
    }

    protected void toggleAnimation() {
        isAnimationOn = !isAnimationOn;
        getAnimationBtn().setText(isAnimationOn ? "Animation" : "No Animation");
    }
}
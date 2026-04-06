package com.example.a2048;

import android.content.Context;
import android.media.SoundPool;
import android.media.AudioAttributes;

public class SoundManager {
    private final SoundPool soundPool;
    private final int swipeSound;
    private final int mergeSound;
    private final int gameOverSound;
    private final int wonSound;
    private boolean enabled = true;

    public SoundManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build();

        // Load sounds from res/raw/
        swipeSound    = soundPool.load(context, R.raw.swipe,    1);
        mergeSound    = soundPool.load(context, R.raw.merge,    1);
        gameOverSound = soundPool.load(context, R.raw.gameover, 1);
        wonSound = soundPool.load(context, R.raw.won, 1);
    }

    public void playSwipe()    { play(swipeSound);    }
    public void playMerge()    { play(mergeSound);    }
    public void playGameOver() {
        if (enabled) soundPool.play(gameOverSound, 2f, 2f, 0, 0, 1.5f);
    }
    public void playWon() { play(wonSound); }

    private void play(int soundId) {
        if (enabled) soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled()              { return enabled;         }

    // Call this in onDestroy to free memory
    public void release() { soundPool.release(); }
}
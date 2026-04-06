package com.example.a2048;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.VolumeShaper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class GameGrid {
    private final GridLayout gridLayout;
    private final Context context;
private final Game game;
    private TextView[][] cellViews;

    private final int marginDp = 5;
    private final int paddingDp = 5;

    public GameGrid(GridLayout gridLayout, Context context, Game game) {
        this.gridLayout = gridLayout;
        this.context = context;
        this.game = game;
    }

    public void build() {
        gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        gridLayout.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);

                        int size = gridLayout.getWidth();
                        android.view.ViewGroup.LayoutParams lp = gridLayout.getLayoutParams();
                        lp.height = size;
                        gridLayout.setLayoutParams(lp);

                        buildCells(size);
                        refresh(); // draw initial game state
                    }
                }
        );
    }

    // Rebuild the cell views from scratch
    private void buildCells(int gridPx) {
        int gridSize = game.getGridSize();
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(gridSize);
        gridLayout.setRowCount(gridSize);
        cellViews = new TextView[gridSize][gridSize];

        int paddingPx = dpToPx(paddingDp) * 2;
        int totalMarginPx = dpToPx(marginDp) * 2 * gridSize;
        int cellPx = (gridPx - paddingPx - totalMarginPx) / gridSize;
        int marginPx = dpToPx(marginDp);

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                TextView cell = new TextView(context);
                cell.setGravity(Gravity.CENTER);
                cell.setTextColor(getCellTextColor(0));
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                cell.setTypeface(null, Typeface.BOLD);
//                cell.setBackground(AppCompatResources.getDrawable(this.context, R.drawable.rounded_corners));
//                cell.setBackground(Drawable.createFromPath("@drawable/rounded_corners"));

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellPx;
                params.height = cellPx;
                params.setMargins(marginPx, marginPx, marginPx, marginPx);
                params.columnSpec = GridLayout.spec(j);
                params.rowSpec = GridLayout.spec(i);
                cell.setLayoutParams(params);

                gridLayout.addView(cell);
                cellViews[i][j] = cell;
            }
        }
    }

    // Sync all cell views with current game state
    public void refresh() {
        int gridSize = game.getGridSize();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int val = game.getCellVal(i, j);
                TextView cell = cellViews[i][j];
                cell.setText(val == 0 ? "" : String.valueOf(val));
                cell.setBackgroundColor(getCellColor(val));
                cell.setTextColor(getCellTextColor(val));
            }
        }
    }

    // Color map matching classic 2048 colors
    private int getCellColor(int val) {
        boolean isDark = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        switch (val) {
            case 2:    return isDark ? Color.parseColor("#3d3a30") : Color.parseColor("#eee4da");
            case 4:    return isDark ? Color.parseColor("#4a4535") : Color.parseColor("#ede0c8");
            case 8:    return isDark ? Color.parseColor("#a0522d") : Color.parseColor("#f2b179");
            case 16:   return isDark ? Color.parseColor("#b84c1a") : Color.parseColor("#f59563");
            case 32:   return isDark ? Color.parseColor("#c43d20") : Color.parseColor("#f67c5f");
            case 64:   return isDark ? Color.parseColor("#b83010") : Color.parseColor("#f65e3b");
            case 128:  return isDark ? Color.parseColor("#a08020") : Color.parseColor("#edcf72");
            case 256:  return isDark ? Color.parseColor("#9a7a18") : Color.parseColor("#edcc61");
            case 512:  return isDark ? Color.parseColor("#947510") : Color.parseColor("#edc850");
            case 1024: return isDark ? Color.parseColor("#8e7008") : Color.parseColor("#edc53f");
            case 2048: return isDark ? Color.parseColor("#886b00") : Color.parseColor("#edc22e");
            default:   return ContextCompat.getColor(context, R.color.default_game_cell_color);
        }
    }
    private int getCellTextColor(int val) {
        boolean isDark = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (isDark) return Color.WHITE;
        // In light mode, 2 and 4 use dark text, everything else white
        return (val == 2 || val == 4) ? Color.parseColor("#776e65") : Color.WHITE;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()
        );
    }
}

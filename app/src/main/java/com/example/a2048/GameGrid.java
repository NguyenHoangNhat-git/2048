package com.example.a2048;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
                cell.setTextColor(Color.WHITE);
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                cell.setTypeface(null, Typeface.BOLD);
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
            }
        }
    }

    // Color map matching classic 2048 colors
    private int getCellColor(int val) {
        switch (val) {
            case 2:    return Color.parseColor("#eee4da");
            case 4:    return Color.parseColor("#ede0c8");
            case 8:    return Color.parseColor("#f2b179");
            case 16:   return Color.parseColor("#f59563");
            case 32:   return Color.parseColor("#f67c5f");
            case 64:   return Color.parseColor("#f65e3b");
            case 128:  return Color.parseColor("#edcf72");
            case 256:  return Color.parseColor("#edcc61");
            case 512:  return Color.parseColor("#edc850");
            case 1024: return Color.parseColor("#edc53f");
            case 2048: return Color.parseColor("#edc22e");
            default:   return ContextCompat.getColor(context, R.color.default_game_cell_color);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()
        );
    }
}

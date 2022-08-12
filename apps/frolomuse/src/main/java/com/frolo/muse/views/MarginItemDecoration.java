package com.frolo.muse.views;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

@Deprecated
public class MarginItemDecoration extends RecyclerView.ItemDecoration  {
    final int v;
    final int h;

    public static MarginItemDecoration createLinear(int h, int v) {
        return new MarginItemDecoration(h, v) {
            @Override
            public void getItemOffsets(@NotNull Rect outRect,
                                       @NotNull View view,
                                       @NotNull RecyclerView parent,
                                       @NotNull RecyclerView.State state) {
                outRect.top = v;
                if (isLastItem(view, parent, state)) {
                    outRect.bottom = v;
                }

                outRect.left = h;
                outRect.right = h;
            }
        };
    }

    public static MarginItemDecoration createGrid(int h, int v) {
        return new MarginItemDecoration(h, v) {
            @Override
            public void getItemOffsets(@NotNull Rect outRect,
                                       @NotNull View view,
                                       @NotNull RecyclerView parent,
                                       @NotNull RecyclerView.State state) {
                outRect.top = v;
                outRect.bottom = v;
                outRect.left = h;
                outRect.right = h;
            }
        };
    }

    MarginItemDecoration(int h, int v) {
        this.h = h;
        this.v = v;
    }

    static boolean isLastItem(View view, RecyclerView parent, RecyclerView.State state) {
        return parent.getLayoutManager().getPosition(view) == state.getItemCount() - 1;
    }
}

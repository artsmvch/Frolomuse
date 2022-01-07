package com.frolo.muse.views;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public abstract class Slider extends RecyclerView.OnScrollListener {
    private final static int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onSlideDown();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onSlideUp();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }
    }

    public void onSlideUp() {
    }

    public void onSlideDown() {
    }
}

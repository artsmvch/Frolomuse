package com.frolo.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;


@UiThread
public final class ViewUtils {

    private static final Rect sTmpRect = new Rect();

    /**
     * Finds the area of the given <code>view</code> that is visible to the user.
     * If the view is not shown (its visibility is INVISIBLE or GONE) or the view is not laid out,
     * then <code>null</code> is returned.
     * @param view from which to find the visible area
     * @return the visible area or null
     */
    @Nullable
    public static Rect getAreaVisibleToUser(@NonNull View view) {

        if (!view.isShown()) {
            return null;
        }

        if (!view.isLaidOut()) {
            return null;
        }

        final WindowManager windowManager =
                (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            // should not happen
            return null;
        }

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        final int screenWidth = displayMetrics.widthPixels;
        final int screenHeight = displayMetrics.heightPixels;

        final Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        final Rect screen = new Rect(0, 0, screenWidth, screenHeight);
        final boolean intersects = rect.intersect(screen);
        if (!intersects) {
            return null;
        }

        return rect;
    }

    /**
     * Finds the percent of the <code>view</code>'s area that is visible to the user.
     * If there is no visible area, then 0f is returned.
     * @param view from which to find the percent of the visible area
     * @return the percent of the visible area
     */
    public static float getPercentOfAreaVisibleToUser(@NonNull View view) {
        final Rect visibleArea = getAreaVisibleToUser(view);

        if (visibleArea == null) {
            return 0f;
        }

        final int viewWidth = view.getMeasuredWidth();
        final int viewHeight = view.getMeasuredHeight();
        final int viewArea = viewWidth * viewHeight;

        return ((float) (visibleArea.width() * visibleArea.height())) / viewArea;
    }

    private ViewUtils() {
    }
}

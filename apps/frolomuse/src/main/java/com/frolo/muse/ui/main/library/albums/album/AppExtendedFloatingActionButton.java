package com.frolo.muse.ui.main.library.albums.album;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.frolo.muse.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;


public class AppExtendedFloatingActionButton extends ExtendedFloatingActionButton {

    private final CoordinatorLayout.Behavior<ExtendedFloatingActionButton> behavior;

    public AppExtendedFloatingActionButton(@NonNull Context context) {
        this(context, null);
    }

    public AppExtendedFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.extendedFloatingActionButtonStyle);
    }

    public AppExtendedFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        behavior = new Behavior<>(getContext());
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior<ExtendedFloatingActionButton> getBehavior() {
        return behavior;
    }

    private static class Behavior<T extends ExtendedFloatingActionButton>
            extends CoordinatorLayout.Behavior<T> {

        private final int toolbarHeight;

        Behavior(Context context) {
            toolbarHeight = (int) (48f * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, T fab, View dependency) {
            return super.layoutDependsOn(parent, fab, dependency) || (dependency instanceof AppBarLayout);
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, T fab, View dependency) {
            boolean returnValue = super.onDependentViewChanged(parent, fab, dependency);
            if (dependency instanceof AppBarLayout) {
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                int fabBottomMargin = lp.bottomMargin;
                int distanceToScroll = fab.getHeight() + fabBottomMargin;
                float ratio = (float)dependency.getY()/(float)toolbarHeight;
                fab.setTranslationY(-distanceToScroll * ratio);
            }
            return returnValue;
        }

    }

}

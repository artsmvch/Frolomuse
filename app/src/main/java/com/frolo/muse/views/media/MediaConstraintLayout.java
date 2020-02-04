package com.frolo.muse.views.media;

import android.content.Context;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.frolo.muse.R;


/**
 * Constraint layout that reflects the state of a media item.
 *
 * The state is controlled with {@link MediaConstraintLayout#setChecked(boolean)} and {@link MediaConstraintLayout#setPlaying(boolean)} methods.
 * E.g. this may be used as a root layout for a media item view in a list,
 * so you can set {@link MediaConstraintLayout#setChecked(boolean)} to true if the item is selected by the user,
 * or you can set {@link MediaConstraintLayout#setChecked(boolean)} to true if the item is being played by a media player.
 *
 * Visually the state is displayed by the background drawable.
 * To create such a drawable, use Selectors. @see {@link MediaConstraintLayout#sMediaStates}.
 */
public class MediaConstraintLayout extends ConstraintLayout {

    private static final int[] sMediaStates = { R.attr.state_item_checked, R.attr.state_item_playing };
    private int mMediaStateIndex = -1; // - 1 means that state is not set

    private boolean mChecked = false;
    private boolean mPlaying = false;

    public MediaConstraintLayout(Context context) {
        super(context);
    }

    public MediaConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void resolvePlayState() {
        if (mChecked) {
            mMediaStateIndex = 0;
        } else if (mPlaying) {
            mMediaStateIndex = 1;
        } else {
            mMediaStateIndex = -1;
        }
        refreshDrawableState();
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            resolvePlayState();
        }
    }

    public void setPlaying(boolean playing) {
        if (mPlaying != playing) {
            mPlaying = playing;
            resolvePlayState();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (mMediaStateIndex != -1) {
            final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

            int [] state = { sMediaStates[mMediaStateIndex] };

            mergeDrawableStates(drawableState, state);

            return drawableState;
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }
}
